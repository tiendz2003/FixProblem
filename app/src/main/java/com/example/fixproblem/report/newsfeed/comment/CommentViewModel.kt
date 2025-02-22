package com.example.fixproblem.report.newsfeed.comment

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.fixproblem.data.model.remote.Comment
import com.example.fixproblem.data.model.remote.CommentReply
import com.example.fixproblem.data.model.remote.NotificationType
import com.example.fixproblem.data.repository.CommentRepository
import com.example.fixproblem.extension.Resource
import com.example.fixproblem.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository:CommentRepository,
    private val auth:FirebaseAuth,
    private val notificationHelper: NotificationHelper,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {
    private val reportId = savedStateHandle.getStateFlow<String?>("reportId",null)
    // Thêm một trigger để reset comments
    private val _resetTrigger = MutableStateFlow(0)
    //track comment đang được reply
    private val _replyingTo = MutableStateFlow<Comment?>(null)
    val replyingTo = _replyingTo.asStateFlow()
    // Track expanded comments
    private val _expandedComments = MutableStateFlow<Set<String>>(emptySet())
    val expandedComments = _expandedComments.asStateFlow()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()
    private val _effect = Channel<CommentEffect>()
    val effect = _effect.receiveAsFlow()

    //Lấy cmt
    @OptIn(ExperimentalCoroutinesApi::class)
    val cmtsWithReplies:StateFlow<PagingData<CommentReply>> = combine(
        reportId,
        _resetTrigger,
        _expandedComments,
    ){reportId,_,expanded ->
        Pair(reportId,expanded)
    }.filterNotNull()
        .flatMapLatest { (reportId,expanded) ->
            commentRepository.getCommentsWithReplies(reportId!!,expanded)
        }.cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val comments:StateFlow<PagingData<Comment>> = combine(
        //khi 1 trong 2 thay đổi -> emit giá trị mới
        reportId,
        _resetTrigger,
    ){ reportId,_ ->
        reportId
    }
        .filterNotNull()
        //Kích hoạt flatMapLatest chạy lại
        .flatMapLatest { reportId -> //lấy id mới nhất
            commentRepository.getCommentForReport(reportId) }//sẽ được gọi lại
        .catch { e ->
            Log.e("CommentViewModel", e.message.toString())
            _uiState.update { it.copy(error = "Không tìm thấy bình luận") }
        }
        //cache cũ sẽ được xóa khi người dùng chuyển trang
        .cachedIn(viewModelScope)
        .stateIn(//Đổi flow -> stateflow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )
    fun processIntent(intent:CommentIntent){
        when(intent){
            is CommentIntent.AddComment -> addComment(intent.text)
            is CommentIntent.AddReply -> addReply(intent.text)
            is CommentIntent.LikeComment -> TODO()
            is CommentIntent.LoadComments -> resetComments()
        }
    }
    fun setReplyingTo(comment:Comment?){
        _replyingTo.value = comment
    }
    fun toggleReplies(cmtId:String){
        _expandedComments.update { expanded->
            if(expanded.contains(cmtId)){
                expanded.minus(cmtId)
            }else{
                expanded.plus(cmtId)
            }
        }
    }

    fun setReportId(reportId: String){
        _uiState.update { it.copy() }
       savedStateHandle["reportId"] = reportId
    }
    fun resetComments() {
        viewModelScope.launch {
            _resetTrigger.emit(_resetTrigger.value + 1)
            //Tăng lên 1 đơn vị
        }
    }
    fun addReply(content:String) {
        Log.e("CommentViewModel", "addReply: $content")
        viewModelScope.launch {
            val parentComment = _replyingTo.value ?: return@launch
            val reportId = reportId.value ?: return@launch
            _uiState.update { it.copy(isSubmitting = true) }
            val reportDoc = commentRepository.commentsCollection.document(reportId).get().await()
            val reportTitle = reportDoc.getString("title")?:""
            val reply = Comment(
                id = UUID.randomUUID().toString(),
                reportId = reportId,
                userId = auth.currentUser?.uid ?: return@launch,
                userName = auth.currentUser?.displayName ?: "Người dùng",
                userPhotoUrl = auth.currentUser?.photoUrl?.toString() ?: "",
                content = content,
                timestamp = System.currentTimeMillis(),
                parentId = parentComment.id,
                replyCount = 0 //KHông cho trả lời nữa
            )
            when (commentRepository.addComment(reply)) {
                is Resource.Error -> {
                    Log.e("CommentViewModel", "không thể thêm Reply: ${reply.id}")
                    _effect.send(CommentEffect.ShowToast("Không thể thêm phản hồi"))
                }
                is Resource.Success -> {
                    Log.e("CommentViewModel", "addReply: ${reply.id}")
                    //Tạo thông báo

                      notificationHelper.createNotification(
                          type = NotificationType.REPLY_COMMENT,
                          receiverId = parentComment.userId,
                          reportId = reportId,
                          reportTitle = reportTitle,
                          commentId = parentComment.id,
                          commentContent = parentComment.content
                      )

                    _expandedComments.update { it + parentComment.id }
                    resetComments()
                    setReplyingTo(null)
                    _effect.send(CommentEffect.ShowToast("Đã thêm phản hồi"))
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }
    private fun addComment(content:String){
        viewModelScope.launch {
            Log.e("CommentViewModel", "addComment: $content")
                _uiState.update { it.copy(isSubmitting = true) }
                val currentReportId = reportId.value ?: return@launch
                val reportDoc = commentRepository.firestore
                    .collection("reports")
                    .document(currentReportId)
                    .get()
                    .await()
                val ownerReportId = reportDoc.getString("userId")?:return@launch
                val reportTitle = reportDoc.getString("title")?:""

                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    reportId = currentReportId,
                    userId = auth.currentUser?.uid?:return@launch,
                    userName = auth.currentUser?.displayName?:"Người dùng",
                    userPhotoUrl = auth.currentUser?.photoUrl?.toString()?:"",
                    content = content,
                    timestamp = System.currentTimeMillis(),
                )
                Log.d("CommentViewModel", "addComment: ${comment.content}")
                when(commentRepository.addComment(comment)){
                    is Resource.Success->{
                        Log.e("CommentViewModel", "không thêm thống báo")
                        //Thêm thông báo
                        notificationHelper.createNotification(
                            type = NotificationType.COMMENT,
                            receiverId = ownerReportId,
                            reportId = currentReportId,
                            reportTitle = reportTitle,
                        )
                        //Tải lại comment
                        resetComments()
                        _effect.send(CommentEffect.ShowToast("Đã đăng bình luận"))
                    }
                    is Resource.Error->{
                        _effect.send(CommentEffect.ShowToast("Không thể đăng bình luận"))
                        _uiState.update { it.copy(error = "Lỗi khi đăng bình luận") }
                    }
                }
                _uiState.update { it.copy(isSubmitting = false) }
        }
    }

}