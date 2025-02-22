package com.example.fixproblem.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.fixproblem.data.model.remote.Report
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReportPagingSource(
    private val firestore:FirebaseFirestore,
    private val pageSize:Long = 5
):PagingSource<DocumentSnapshot, Report>() {
    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Report>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Report> {
        return try{
            //Tạo query
            var query = firestore.collection("reports")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .limit(pageSize)
            //Nếu có key,thì load từ document đó
            if(params.key != null){
                query = query.startAfter(params.key!!)
            }
            //Thực hiện query
            val querySnapshot = query.get().await()
            val reports =querySnapshot.documents.mapNotNull { document->
                document.toObject(Report::class.java)?.copy(id = document.id)
            }
            //Lấy key cho trang tiếp theo
            val lastVisible = querySnapshot.documents.lastOrNull()
            LoadResult.Page(
                data = reports,
                prevKey = null,
                nextKey = lastVisible
            )
        }catch (e:Exception){
            LoadResult.Error(e)
        }
    }

}