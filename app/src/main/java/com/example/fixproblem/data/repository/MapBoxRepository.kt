package com.example.fixproblem.data.repository

import android.util.Log
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.di.AppDispatcher
import com.example.fixproblem.di.DispatcherType
import com.example.fixproblem.extension.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MapBoxRepository @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val searchEngine: SearchEngine,
    private val firestore: FirebaseFirestore,
    private val mapboxNavigation:MapboxNavigation,
    @AppDispatcher(DispatcherType.IO) val ioDispatcher:CoroutineDispatcher
) {

    private val _location = MutableStateFlow<Point?>(null)
    val location get() = _location.asStateFlow()
    //Cập nhật vị trí hiện tại của người dùng
    suspend fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.await()?.let { location ->
                _location.emit(Point.fromLngLat(location.longitude, location.latitude))
            }
        }catch (e:SecurityException){
            Log.d("Location","Lỗi khi lấy địa chỉ: ${e.message}")
        }
    }
    //Lấy danh sách vị trí từ firestore
    fun getLocations() = callbackFlow {
        val locationsRef = firestore.collection("reports")

        val listener = locationsRef.addSnapshotListener{snapshot,error->
            if(error != null){
                close(error)
                return@addSnapshotListener
            }
           if(snapshot != null){
               val reports = snapshot.documents.mapNotNull { doc->
                   val locations = doc.getGeoPoint("location")?:return@mapNotNull null
                   Report(
                       id = doc.id,
                       location = locations,
                       title = doc.getString("title")?:"",
                       description = doc.getString("description")?:"",
                       userName = doc.getString("userName")?:"",
                       userImage = doc.getString("userImage")?:"",
                       timestamp = doc.getLong("timestamp")?:0,
                       imageUrl = doc.getString("imageUrl")?:""
                       )
               }

               trySend(reports)
           }
        }
        awaitClose { listener.remove() }
    }.retry(3) { e->
        e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE
    }.flowOn(ioDispatcher)

    //Chuyển đổi vị trí sang địa chỉ
    fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double,
    ) = callbackFlow {
         var searchRequestTask: AsyncOperationTask? = null
         try {
             val reverseGeoOptions = ReverseGeoOptions(
                 center = Point.fromLngLat(longitude,latitude),
                 limit = 1,
                 languages = listOf(IsoLanguageCode("en"))
             )
             val searchCallBack = object : SearchCallback {
                 override fun onError(e: Exception) {
                     trySend(Result.failure(e))
                 }

                 override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                     if(results.isNotEmpty()){
                         val result = results[0]
                         Log.d("Address",result.address.toString())
                         val fullAddress = buildString {
                             result.address?.street?.let { append(it) }
                             result.address?.neighborhood?.let { append(", $it") }
                             result.address?.district?.let { append(", $it") }
                             result.address?.place?.let { append(", $it") }
                             result.address?.region?.let { append(", $it") }
                             result.address?.country?.let { append(", $it") }
                         }
                         trySend(Result.success(fullAddress.ifEmpty { "Không tìm thấy địa chỉ" }))
                     }else{
                         trySend(Result.failure(Exception("Không tìm thấy địa chỉ")))
                     }
                 }
             }
             searchRequestTask = searchEngine.search(reverseGeoOptions,searchCallBack)
         }catch (e:Exception){
             trySend(Result.failure(e))
             close(e)
         }
        awaitClose {
            searchRequestTask?.cancel()
        }
    }.catch { e->
        emit(Result.failure(e))
    }.flowOn(Dispatchers.IO)

    suspend fun startNavigation(
        origin: Point,
        destination: Point,
        onRouteReady: (NavigationRoute) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(ioDispatcher) {
            try {
                val routeOptions = RouteOptions.builder()
                    .applyDefaultNavigationOptions()
                    .coordinatesList(listOf(origin, destination))
                    .alternatives(true)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .build()

                mapboxNavigation?.requestRoutes(
                    routeOptions,
                    object : NavigationRouterCallback {
                        override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            routerOrigin: String
                        ) {
                            val primaryRoute = routes.first()
                            mapboxNavigation?.setNavigationRoutes(routes)
                            mapboxNavigation?.startTripSession()
                            onRouteReady(primaryRoute)
                        }

                        override fun onFailure(
                            reasons: List<RouterFailure>,
                            routeOptions: RouteOptions
                        ) {
                            onError("Không tìm thấy đường đi: $reasons")
                        }

                        override fun onCanceled(
                            routeOptions: RouteOptions,
                            routerOrigin: String
                        ) {
                            onError("Hủy dẫn đường")
                        }
                    }
                )
            } catch (e: SecurityException) {
                onError(e.message ?: "Security error")
            }
        }
    }
    fun stopNavigation() {
        mapboxNavigation.stopTripSession()
    }

    suspend fun searchQuery(
        query:String,
        limit:Int = 5,
        countryCode: IsoCountryCode = IsoCountryCode.VIETNAM
    ): Resource<List<SearchSuggestion>> = suspendCoroutine { continuation->
        searchEngine.search(
            query = query,
            options = SearchOptions(
                limit = limit,//giới hạn hiển thị kết quả
                countries = listOf(countryCode)//Vùng tìm kiếm
            ),
            callback = object : SearchSuggestionsCallback {
                override fun onError(e: Exception) {
                    continuation.resume(Resource.Error(e))
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    continuation.resume(Resource.Success(suggestions))
                }
            }
        )
    }
}