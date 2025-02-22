package com.example.fixproblem.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.fixproblem.data.model.remote.Comment
import com.example.fixproblem.data.model.remote.CommentReply
import com.example.fixproblem.data.repository.paging.CommentPagingSource
import com.example.fixproblem.data.repository.paging.CommentWithRepliesPagingSource
import com.example.fixproblem.di.AppDispatcher
import com.example.fixproblem.di.DispatcherType
import com.example.fixproblem.extension.Resource
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepository @Inject constructor(
    val firestore: FirebaseFirestore,
    @AppDispatcher(DispatcherType.IO) val ioDispatcher: CoroutineDispatcher
) {
    val commentsCollection = firestore.collection("comments")
    //Thêm comment
    suspend fun addComment(comment:Comment):Resource<Comment>{
        return try{
            val newComment = comment.copy(id = commentsCollection.document().id)
            if(comment.parentId != null){
                firestore.runTransaction {transaction->
                    val parentRef = commentsCollection.document(comment.parentId)
                    val parentDoc = transaction.get(parentRef)
                    if(parentDoc.exists()){
                        //Cập nhật replycount
                        val currentReplyCount = parentDoc.getLong("replyCount")?:0
                        transaction.update(parentRef,"replyCount",currentReplyCount+1)
                    }
                }.await()
            }

            commentsCollection.document(newComment.id).set(newComment).await()
            Resource.Success(newComment)
        }catch (e:Exception){
            Log.e("CommentRepository",e.message.toString())
            Resource.Error(e)
        }
    }
    //deleteCommment
    //lấy likeCount
    suspend fun getLikeCount(commentId:String):Int{
        return try {
            val replies = firestore.collection("comments")
                .whereEqualTo("parentId",commentId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            replies.count.toInt()
        }catch (e:Exception){
            0
        }
    }

    //load comment
    fun getCommentForReport(reportId:String,pageSize:Int = 10):Flow<PagingData<Comment>>{
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize * 2
            ),
            pagingSourceFactory = {
                CommentPagingSource(
                    firestore,
                    reportId
                )
            }
        ).flow.flowOn(ioDispatcher)
    }
    //load cmt và reply đồng thời
    fun getCommentsWithReplies(reportId: String,expandedComments: Set<String>,pageSize:Int = 10):Flow<PagingData<CommentReply>>{
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize*2
            ),
            pagingSourceFactory = {
                CommentWithRepliesPagingSource(
                    firestore = firestore,
                    reportId = reportId,
                    expandedComments = expandedComments
                )
            }
        ).flow.flowOn(ioDispatcher)
    }
}