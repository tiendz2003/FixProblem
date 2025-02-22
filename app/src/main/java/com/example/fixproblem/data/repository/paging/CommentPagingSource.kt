package com.example.fixproblem.data.repository.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.fixproblem.data.model.remote.Comment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CommentPagingSource (
    private val firestore: FirebaseFirestore,
    private val reportId: String
):PagingSource<DocumentSnapshot,Comment>() {
    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Comment>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Comment> {
       return try {
           // Tạo query với filter theo reportId
           var query = firestore.collection("comments")
               .whereEqualTo("reportId",reportId)
               .orderBy("timestamp",Query.Direction.DESCENDING)
               .limit(params.loadSize.toLong())
           //Nếu có key,thì load từ document đó
           if(params.key != null){
               query = query.startAfter(params.key!!)
           }
           //Thực hiện query
           val querySnapshot = query.get().await()
           val comments = querySnapshot.documents.mapNotNull { document->
               document.toObject(Comment::class.java)?.copy(id = document.id)
           }
           //Lấy key cho trang tiếp theo
           val lastVisible = querySnapshot.documents.lastOrNull()
           LoadResult.Page(
               data = comments,
               prevKey = null,//chỉ tải phía trước
               nextKey = lastVisible
           )
       }catch (e:Exception){
           Log.e("CommentPaging",e.message.toString())
            LoadResult.Error(e)
       }
    }

}