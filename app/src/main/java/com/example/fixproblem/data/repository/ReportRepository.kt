package com.example.fixproblem.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.fixproblem.data.database.AppDatabase
import com.example.fixproblem.data.model.local.toReport
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.data.repository.paging.ReportPagingSource
import com.example.fixproblem.data.repository.paging.ReportRemoteMediator
import com.example.fixproblem.di.AppDispatcher
import com.example.fixproblem.di.DispatcherType
import com.example.fixproblem.extension.Resource
import com.example.fixproblem.utils.ImageCompressor
import com.example.fixproblem.utils.NetworkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ReportRepository @Inject constructor(
    private val database: AppDatabase,
    val firestore:FirebaseFirestore,
    private val storage:FirebaseStorage,
    private val imageCompressor: ImageCompressor,
    private val networkManager: NetworkManager,
    @AppDispatcher(DispatcherType.IO) val ioDispatcher:CoroutineDispatcher
) {
    val reportsCollection = firestore.collection("reports")
    private val usersCollection = firestore.collection("users")

    //Tải ảnh lên
    suspend fun uploadImage(imageUri:Uri,onProgress: (Float) -> Unit):String{
        return withContext(ioDispatcher){
            // Nén ảnh
            val compressedImage = imageCompressor.compressImage(
                imageUri = imageUri,
                maxWidth = 1080,
                maxHeight = 1080,
                quality = 80,
            )
            val filename = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("report_images/$filename")
            try {
                //Upload ảnh lên firebase storage
                storageRef.putBytes(compressedImage).addOnProgressListener {taskSnapShot->
                    //Quan sát quá trình upload
                    val progress = taskSnapShot.bytesTransferred.toFloat() / taskSnapShot.totalByteCount.toFloat()
                    onProgress(progress)
                }.await()
                storageRef.downloadUrl.await().toString()
            }catch (e:Exception){
                throw  Exception("Lỗi khi tải ảnh lên :${e.message}",e)
            }
        }

    }
    //Tạo bài viết
    suspend fun createReport(report: Report){
        withContext(ioDispatcher){
            try {
                reportsCollection.document(report.id).set(report).await()
            }catch (e:Exception){
                Log.d("Firestore","Lỗi khi tạo bài viết:${e.message}",e)
                throw  e
            }
        }
    }
    @OptIn(ExperimentalPagingApi::class)
    fun getPageReports():Flow<PagingData<Report>>{
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                prefetchDistance = 3,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            remoteMediator = ReportRemoteMediator(
                firestore = firestore,
                reportDb = database,
                networkManager = networkManager
            ),
            pagingSourceFactory = {
              database.reportDao.getAllReports()
            }
        ).flow.map { pagingData->
            pagingData.map {
              it.toReport()
            }
        }.flowOn(ioDispatcher)
    }
    //Prefetch image
    suspend fun prefetchImages(reports: List<Report>, context:Context){
        withContext(ioDispatcher){
            reports.forEach { report->
                val request = ImageRequest.Builder(context)
                    .data(report.imageUrl)
                    .memoryCacheKey(report.imageUrl)
                    .diskCacheKey(report.imageUrl)
                    .size(width = 800, height = 600)
                    .build()
                // Kiểm tra xem ảnh đã được load trước đó chưa
                val result = ImageLoader(context).execute(request)
                // Thực hiện prefetch không đồng bộ
                // Nếu ảnh chưa được load thành công, thực hiện prefetch
                if (result !is SuccessResult) {
                    ImageLoader(context).enqueue(request).job.await()
                }
            }
        }
    }
    //danh sách bài viết
     fun getReports(limit:Long = 20) =
        callbackFlow {
            val listener = reportsCollection
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener{ snapshot, error ->
                    if(error != null){
                        close(error)
                        return@addSnapshotListener
                    }
                    val reports = snapshot?.documents?.mapNotNull {result ->
                        result.toObject(Report::class.java)
                    }?: emptyList()
                    trySend(reports)
                }
                awaitClose { listener.remove() }
        }.retry(3) { e->
            //thử lại với lỗi là exception
            e is Exception
        }
            .catch {
            emit(emptyList())
        }.flowOn(ioDispatcher)

    suspend fun getReportById(reportId: String):Resource<Report>{
        return withContext(ioDispatcher){
            try {
                val documentSnapshot = reportsCollection.document(reportId).get().await()
                if(documentSnapshot.exists()){
                    val report = documentSnapshot.toObject(Report::class.java)
                    report?.let {
                       Resource.Success(it)
                    }?: Resource.Error(Exception("Không tìm thấy bài viết với id: $reportId"))
                }else{
                    Resource.Error(Exception("Bài viết không tìm thấy"))
                }
            }catch (e:Exception){
                Resource.Error(e)
            }
        }
    }
   /* suspend fun handleLikeReport(reportId: String, liked: Boolean) {
        return withContext(Dispatchers.IO){
            val reportRef = firestore
        }
    }*/
}