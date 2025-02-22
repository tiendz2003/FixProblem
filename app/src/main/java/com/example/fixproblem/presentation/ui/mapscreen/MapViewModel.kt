package com.example.fixproblem.presentation.ui.mapscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixproblem.data.repository.MapBoxRepository
import com.example.fixproblem.data.repository.ReportRepository
import com.example.fixproblem.extension.Resource
import com.mapbox.bindgen.Expected
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.NavigationRouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationManager:MapBoxRepository,
    private val reportRepository:ReportRepository,
    private var mapboxNavigation: MapboxNavigation?,
    mapboxRouteLineViewOptions: MapboxRouteLineViewOptions,
):ViewModel() {
    private val _mapState = MutableStateFlow(MapState())
    val mapState get()= _mapState.asStateFlow()
    //Sử dụng Flow để stream vị trí người dùng
    private val _userLocation = MutableStateFlow<Point?>(null)
    val userLocation: StateFlow<Point?> = _userLocation.asStateFlow()
    // Thêm các states mới
    private val _routeLine = MutableStateFlow<LineString?>(null)
    val routeLine = _routeLine.asStateFlow()

    val _progress = MutableStateFlow(0.0)
    val progress = _progress.asStateFlow()
    private val queryFlow = MutableSharedFlow<String>(1)
    private val _lightPreset = MutableStateFlow(LightPresetValue.DAY)
    val lightPreset = _lightPreset.asStateFlow()
    private val _routeLineStyle = MutableStateFlow<Expected<RouteLineError, RouteSetValue>?>(null)
    val routeLineStyle get() = _routeLineStyle.asStateFlow()
    private var routeLineApi:MapboxRouteLineApi?=null
    private var routeLineView: MapboxRouteLineView?=null
    //Cung cấp vị trí hiện tại của nguời dùng trên tuyến đường
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
            _progress.value = routeProgress.distanceTraveled.toDouble()
            _mapState.value = _mapState.value.copy(routeProgress = routeProgress)
            routeProgress.currentState.let {
                viewModelScope.launch {
                    val routeLineData = mapboxNavigation?.getNavigationRoutes()?.map { route->
                        NavigationRouteLine(
                            route = route,
                            identifier = route.routeIndex.toString(),
                        )
                    }
                    routeLineApi?.setNavigationRouteLines(
                        newRoutes = routeLineData!!
                    ){ value ->
                        _routeLineStyle.value = value
                    }
                }
            }
        }
    //->Khi các bro đi lạc lối,chệch tuyến đường->Lắng  nghe thay đổi tuyến đường
    private val routesObserver = RoutesObserver { routes ->
        val primaryRoute = routes.navigationRoutes.firstOrNull()
        _mapState.update { it.copy(navigationRoutes = primaryRoute) }
    }
    init {
        viewModelScope.launch {
            //Cold flow chỉ lấy vị trí người dùng khi collect
            locationManager.location.collect{location->
                //Nhận giá trị vừa phát ra
                _userLocation.emit(location)
            }

        }
        setupSearch()
        getLocations()
        mapboxNavigation?.registerRoutesObserver(routesObserver)
        mapboxNavigation?.registerRouteProgressObserver(routeProgressObserver)

        val routeLineApiOptions = MapboxRouteLineApiOptions
            .Builder().vanishingRouteLineEnabled(true).build()
        routeLineApi = MapboxRouteLineApi(routeLineApiOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineViewOptions)

    }
    fun processIntent(intent:MapIntent) {
           when (intent) {
               is MapIntent.LoadReportById -> getReportById(intent.id)
               is MapIntent.NavigationError -> {

               }
               is MapIntent.RouteUpdated -> {
                   _mapState.update { it.copy(
                       navigationRoutes = intent.route,
                       isNavigating = true
                   ) }
               }
               is MapIntent.StartNavigation -> {
                   startNavigation(intent.origin,intent.destination)
               }
               is MapIntent.StopNavigation -> stopNavigation()
               is MapIntent.UpdateCurrentLocation -> getLocations()
               is MapIntent.ClearSearch -> {
                   _mapState.update { it.copy(
                       searchQuery = "",
                       error = null,
                       selectedLocation = null,
                       searchResults = emptyList(),
                       isSearching = false,
                       showSearchResult = true
                   // Reset error khi user thay đổi query
                   )}
               }
               is MapIntent.SelectLocation -> {
                   val suggestion = intent.location
                   _mapState.update {
                       it.copy(
                           selectedLocation = suggestion,
                           searchResults = emptyList(),
                           searchQuery = intent.location.address,
                           error = null,
                           isSearching = false,
                           showSearchResult = false
                       )
                   }
               }
               //Thực hiện chức năng tìm kiếm
               is MapIntent.UpdateQuery -> {
                   Log.d("MapViewModel", "UpdateQuery: ${intent.query}")
                   _mapState.update { it.copy(isSearching = true,showSearchResult = true,searchQuery = intent.query) }
                   viewModelScope.launch {
                       queryFlow.emit(intent.query)
                   }
               }

       }
    }
    //Lấy vị trí hiện tại
    private fun getLocations(){
        viewModelScope.launch {
                locationManager.getLocations()
                    .catch {error->
                        _mapState.update { currentState->
                            currentState.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    }.collectLatest{reports->
                        _mapState.update { currentState->
                            currentState.copy(
                                isLoading = false,
                                locations = reports.map { it.location },
                                reports = reports
                            )
                        }
                    }
        }
    }
    private fun startNavigation(origin:Point,destination:Point) {
        viewModelScope.launch {
            locationManager.startNavigation(
                origin,
                destination,
                onRouteReady = { primaryRoute ->
                    //Trả về một chuỗi mã hóa polyline đại diện cho hình dạng của tuyến đường.
                    primaryRoute.directionsRoute.geometry()?.let { geometry->
                        //LineString.fromPolyline(geometry, Constants.PRECISION_6)
                        // chuyển đổi chuỗi polyline này thành một đối tượng LineString.
                        val lineString = LineString.fromPolyline(
                            geometry,
                            Constants.PRECISION_6
                        )
                        _routeLine.value = lineString
                    }
                    _mapState.update { it.copy(isLoading = false, isNavigating = true, navigationRoutes = primaryRoute) }
                },
                onError = { error ->
                    _mapState.update { it.copy(isLoading = false, error = error) }
        })
    }
    }

    private fun stopNavigation(){
       viewModelScope.launch {
           locationManager.stopNavigation()
           _mapState.update {
               it.copy(
                   isFollowingUser = false,
                   isNavigating = false,
                   navigationRoutes = null
               )
           }
       }
    }
    fun getCurrentLocation(){
        viewModelScope.launch {
            locationManager.getCurrentLocation()
        }
    }
    fun getReportById(reportId:String){
        viewModelScope.launch {
            _mapState.update { it.copy(isLoading = true) }
            when(val report = reportRepository.getReportById(reportId)){
                is Resource.Error -> _mapState.update { it.copy(isLoading = false, error = report.exception.message) }
                is Resource.Success -> _mapState.update { it.copy(isLoading = false, selectedReport = report.data) }
            }
            _mapState.update { it.copy(isLoading = false) }
        }
    }
    @OptIn(FlowPreview::class)
    fun setupSearch(){
        Log.d("MapViewModel", "setupSearch:${queryFlow} ")
        viewModelScope.launch {
            queryFlow
                .debounce(500)
                .distinctUntilChanged()
                .filterNot { it.isBlank() }
                .onEach { query->
                    Log.d("MapViewModel", "Processing query: $query")
                    _mapState.update { it.copy(isLoading = true) }
                    when(val result = locationManager.searchQuery(query)){
                        is Resource.Error -> {
                            _mapState.update { it.copy(isLoading = false, isSearching = false,error = result.exception.message) }
                        }
                        is Resource.Success ->{
                            Log.d("MapViewModel", "setupSearch:success ${result.data.size}")
                            _mapState.update { it.copy(
                                isLoading = false,
                                isSearching = false,
                                searchResults = result.data) }
                        }
                    }
                }
                .catch {e->
                    _mapState.update{ it.copy(error = e.message, isLoading = false) }
                }
                .collect()
        }
    }
    fun clearSelectedReport(){
        _mapState.update { it.copy(selectedReport = null) }
    }
     fun updateCameraPosition(position: Point, zoom:Double) {
         _mapState.update {currentState->
             currentState.copy(
                 cameraCenter = position,
                 zoomLevel = zoom
             )
         }
     }
    fun toggleLightPreset() {
        _lightPreset.value = if (_lightPreset.value == LightPresetValue.DAY) {
            LightPresetValue.NIGHT
        } else {
            LightPresetValue.DAY
        }
    }
    fun toggleCameraMode() {
        _mapState.update { currentState ->
            currentState.copy(isFollowingUser = !currentState.isFollowingUser)
        }
    }
    override fun onCleared() {
        super.onCleared()
        //Giải phóng tài nguyên
        mapboxNavigation?.stopTripSession()
        mapboxNavigation?.unregisterRoutesObserver(routesObserver)
        mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation = null
        routeLineApi?.cancel()
        routeLineView?.cancel()
    }

}