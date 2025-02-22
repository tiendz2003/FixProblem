package com.example.fixproblem.data.repository.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.fixproblem.data.model.remote.Comment
import com.example.fixproblem.data.model.remote.CommentReply
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentWithRepliesPagingSource @Inject constructor(
    private val firestore:FirebaseFirestore,
    private val reportId:String,
    private val expandedComments:Set<String>
):PagingSource<QuerySnapshot,CommentReply>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, CommentReply>): QuerySnapshot? {
       return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, CommentReply> {
        return try {
            //tải comment gốc
            val baseQuery = firestore.collection("comments")
                .whereEqualTo("reportId",reportId)
                .whereEqualTo("parentId",null)//CHỉ lấy comment gốc
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .limit(params.loadSize.toLong())

            val currentPage = params.key ?: baseQuery.get().await()//nếu không key lấy trang đầu tiên

            val lastDocumentSnapshot = currentPage.documents.lastOrNull()//lưu đocument cuối cùng
            val nextPage = if(lastDocumentSnapshot != null){
                baseQuery.startAfter(lastDocumentSnapshot).get().await()
            }else{
                null
            }

            val commentsWithReply = currentPage.documents.mapNotNull { documentSnapshot ->
                val comment = documentSnapshot.toObject(Comment::class.java)?:return@mapNotNull null
                // Chỉ tải replies nếu comment đó đang được expanded
                val replies = if(expandedComments.contains(comment.id)){
                    firestore.collection("comments")
                        .whereEqualTo("reportId",reportId)
                        .whereEqualTo("parentId",comment.id)
                        .orderBy("timestamp",Query.Direction.ASCENDING)//cũ nhất lên đầu
                        .get().await().documents.mapNotNull {
                            it.toObject(Comment::class.java)
                        }
                }else{
                    emptyList()
                }
                CommentReply(
                    comment = comment,
                    replies = replies,
                    isExpanded = expandedComments.contains(comment.id)
                )
            }
            LoadResult.Page(
                data = commentsWithReply,//dữ liệu trang hiện tại
                prevKey = null,
                nextKey = nextPage//snapshot cho trang tiếp theo
            )

        }catch (e:Exception){
            Log.e("CommentWithRepliesPagingSource",e.message.toString())
            LoadResult.Error(e)
        }
    }


}