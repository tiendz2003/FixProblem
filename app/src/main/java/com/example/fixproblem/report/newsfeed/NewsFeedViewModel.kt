package com.example.fixproblem.report.newsfeed

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.flatMap
import androidx.paging.map
import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.data.model.remote.NotificationType
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.data.repository.NotificationRepository
import com.example.fixproblem.data.repository.ReportRepository
import com.example.fixproblem.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewsFeedViewModel @Inject constructor(
    private val reportRepository:ReportRepository,
    private val notificationHelper: NotificationHelper,
    private val auth:FirebaseAuth,
    private val context: Application,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {
    private val _uiState = MutableStateFlow(NewsFeedState())
    val uiState: StateFlow<NewsFeedState> get() = _uiState.asStateFlow()

    private val _effect = Channel<NewsFeedEffect>()
    val effect get() = _effect.receiveAsFlow()
    private val usersRef =reportRepository.firestore.collection("users")
        .document(auth.uid!!)
    private val likedReportsRef  = usersRef.collection("likedReports")

    //PagingFlow
    private val _pagedReports = MutableStateFlow<PagingData<Report>>(PagingData.empty())
    private val currentFilter = savedStateHandle.getStateFlow("current_filter", "Tất cả")
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedReports: StateFlow<PagingData<Report>> = currentFilter.flatMapLatest { filter->
        reportRepository.getPageReports()
            .map { pagingData ->
                pagingData.map { report->
                    val likedReportIds = likedReportsRef.get().await().documents.map { it.id }
                    report.copy(isLiked = report.id in likedReportIds)
                }.let {reportData->
                    when(filter){
                        "Tất cả "->{
                            reportData
                        }
                        "Trong tuần"->{
                            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                            reportData.filter { it.timestamp >= weekAgo }
                        }
                        "Nổi bật"->{
                            reportData.filter { it.like > 0 }
                        }
                        "Mới nhất" -> reportData
                        else -> reportData
                    }
                }
            }
    }.cachedIn(viewModelScope).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),//giữ lại dữ  liệu sau khi không ai collect
        initialValue = PagingData.empty()
    )
    fun processIntent(intent:NewsFeedIntent) {
        when(intent){
            is NewsFeedIntent.CommentReport -> TODO()
            is NewsFeedIntent.GetReport -> {}
            is NewsFeedIntent.LikeReport -> handleLikeReport(intent.reportId,intent.isLiked)
            is NewsFeedIntent.ShareReport -> TODO()
            is NewsFeedIntent.UpdateCategory -> filterReport(intent.category)
            is NewsFeedIntent.PrefetchImages -> prefetchImages(intent.reports)
        }
    }

    private fun prefetchImages(reports:List<Report>){
        viewModelScope.launch {
            try {
                reportRepository.prefetchImages(reports, context = context)
            }catch (e:Exception){
                Log.d("NewsFeed", "prefetchImages: ${e.message}")
            }
        }
    }


    private fun filterReport(category: String){
        viewModelScope.launch {
            savedStateHandle["current_filter"] = category
        }
    }
     private fun handleLikeReport(reportId: String, isLiked:Boolean){
        viewModelScope.launch {
            try {
                val reportDocs = reportRepository.reportsCollection
                    .document(reportId)
                    .get()
                    .await()
                val authorId = reportDocs.getString("userId")
                val reportTitle = reportDocs.getString("title") ?: ""
                if(isLiked && authorId != null && authorId != auth.uid){
                    //Tạo thông báo Like
                    notificationHelper.createNotification(
                        type = NotificationType.LIKE,
                        receiverId = authorId,
                        reportId = reportId,
                        reportTitle = reportTitle
                    )
                }
                if(isLiked){
                    //Thêm vào danh sách đã thích của người dùng
                    likedReportsRef.document(reportId).set(mapOf("timestamp" to System.currentTimeMillis()))
                }else{
                    // Bỏ like: Xóa khỏi danh sách like của user
                    likedReportsRef.document(reportId).delete()
                }
                // Cập nhật số lượt like trên bài viết
                reportRepository.updateLike(reportId,isLiked)
                val currentPagingData = _pagedReports.value
                //Cập nhật state local
                val updatedPagingData  = currentPagingData.map {post->
                    if(post.id == reportId){
                        post.copy(like = if(isLiked) post.like + 1 else post.like - 1)
                    }else{
                        post
                    }
                }
                _pagedReports.value = updatedPagingData
                //_uiState.value = _uiState.value.copy(posts = updates)
                _effect.send(NewsFeedEffect.ShowToast(
                    if(isLiked){
                        "Đã thích bài viết"
                    }else{
                        "Đã bỏ thích bài viết"
                    }
                ))
            }catch (e:Exception){
                Log.e("NewsFeedViewModel", "handleLikeReport: ${e.message}",e)
                _effect.send(NewsFeedEffect.ShowToast("Lỗi khi bày tỏ cảm xúc"))
            }
        }
    }
    private fun handleCommentReport(reportId: String){
        viewModelScope.launch {
            try {
                _effect.send(NewsFeedEffect.NavigateToDetail(reportId))
            }catch (e:Exception){
                _effect.send(NewsFeedEffect.ShowToast("Không thể mở bình luận"))
            }
        }
    }

    //Hàm cập nhật lượt like
    private fun ReportRepository.updateLike(reportId: String,isLiked: Boolean){
        viewModelScope.launch {
            val reportRef  = firestore.collection("reports").document(reportId)
            reportRef.update("like",FieldValue.increment(
                if(isLiked) 1 else -1
            ))
        }
    }
}