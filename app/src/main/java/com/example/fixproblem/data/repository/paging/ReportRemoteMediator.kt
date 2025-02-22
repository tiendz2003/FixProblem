package com.example.fixproblem.data.repository.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.util.query
import androidx.room.withTransaction
import com.example.fixproblem.data.database.AppDatabase
import com.example.fixproblem.data.model.local.RemoteKey
import com.example.fixproblem.data.model.local.ReportEntity
import com.example.fixproblem.data.model.local.toReportEntity
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.utils.NetworkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class ReportRemoteMediator(
    private val firestore: FirebaseFirestore,
    private val reportDb:AppDatabase,
    private val pageSize: Long = 10,
    private val networkManager: NetworkManager
    ) : RemoteMediator<Int,ReportEntity>() {

    private val reportDao = reportDb.reportDao
    private val remoteKeyDao = reportDb.remoteKeyDao
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ReportEntity>
    ): MediatorResult {
        return try {
            //Kiểm tra kết nối internet
            if(!networkManager.isNetworkAvailable()){
                return MediatorResult.Success(endOfPaginationReached = true)
            }


           val key =when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                   //Lấy item cuối cùng và key của nó
                    val lastItem = state.lastItemOrNull()
                        ?:return MediatorResult.Success(endOfPaginationReached = true)

                    val remoteKey = remoteKeyDao.getRemoteKey(lastItem.id)
                        ?:return MediatorResult.Success(endOfPaginationReached = true)

                    remoteKey.nextKey
                }
            }

            // Thực hiện query
            var query = firestore.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize)
            if(key != null){
                val startAfterDoc= firestore.collection("reports").document(key).get().await()
                query = query.startAfter(startAfterDoc)
            }
            val querySnapshot = query.get().await()
            val documents = querySnapshot.documents

            val reports = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Report::class.java)?.copy(id = document.id)
            }
            val reportEntities = reports.map {
                it.toReportEntity()
            }
            //Tạo remoteKeys cho từng document
            val remoteKeys = reports.map { report->
                RemoteKey(
                    id = report.id,
                    prevKey = null,
                    nextKey = documents.lastOrNull()?.id
                )
            }

            // Lưu vào database trong một transaction
            reportDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    reportDao.deleteAllReports()
                    remoteKeyDao.deleteAllRemoteKeys()
                }
                remoteKeyDao.insertAll(remoteKeys)
                reportDb.reportDao.upsertReport(reportEntities)
            }
            MediatorResult.Success(
                endOfPaginationReached = reports.isEmpty()
            )
        } catch (e: Exception) {
            Log.e("ReportMediator", "Lỗi khi tải:", e)
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return if (networkManager.isNetworkAvailable()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}