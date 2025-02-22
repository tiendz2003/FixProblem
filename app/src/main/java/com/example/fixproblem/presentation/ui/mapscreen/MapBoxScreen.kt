package com.example.fixproblem.presentation.ui.mapscreen

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fixproblem.R
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.extension.AnimatedContainer
import com.example.fixproblem.extension.formatDistance
import com.example.fixproblem.extension.formatDuration
import com.example.fixproblem.extension.formatTimestamp
import com.example.fixproblem.presentation.theme.CustomColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleListValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MapViewModel = hiltViewModel(),
    navController: NavController,
) {
        val mapState by viewModel.mapState.collectAsStateWithLifecycle()
        val routeLineStyle by viewModel.routeLineStyle.collectAsStateWithLifecycle()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
        )
        val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
        val scope= rememberCoroutineScope()
        val context = LocalContext.current
        //Quản lý trạng thái quyền
        val locationPermissionsState = rememberPermissionState(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
        )
        val mapViewportState = rememberMapViewportState{
            setCameraOptions {
                center(userLocation)
                zoom(0.0)
                pitch(0.0)
                bearing(0.0)
            }
        }

        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            when(locationPermissionsState.status){
                //Xử lý khi cấp quyền thành công
                is PermissionStatus.Granted -> {
                    //Khi màn hình khởi động,ngay lập tức lấy vị trí hiện tại của người dùng
                    LaunchedEffect(Unit) {
                        Log.d("MapDebug", "Cấp quyền thành công,đang lấy vị trí hiện tại")
                        viewModel.getCurrentLocation()

                        Log.d("MapDebug", "Current location: $userLocation")
                    }
                    //Chuyển vị trí người dùng sang tọa độ
                    userLocation?.let {point->
                        Log.d("MapDebug", "Đang hiển thị bản đồ : $point")
                        BottomSheetScaffold(
                            scaffoldState = bottomSheetScaffoldState,
                            sheetContent = {
                                when{
                                    mapState.isLoading ->{
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    mapState.error != null -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mapState.error ?:"Lỗi trong quá trình tải",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    mapState.isFollowingUser ->{
                                        mapState.routeProgress?.let {routeProgress->
                                            TripProgressView(
                                                routeProgress = routeProgress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp),
                                                context = context,
                                                onPositioning ={
                                                    mapViewportState.transitionToFollowPuckState()
                                                },
                                                onDismiss = {
                                                    viewModel.processIntent(MapIntent.StopNavigation)
                                                }
                                            )
                                        }
                                    }
                                    mapState.selectedReport != null->{
                                        MapBoxBottomSheet(
                                            currentLocation = point,
                                            report = mapState.selectedReport!!,
                                            viewModel = viewModel
                                        ) {
                                            scope.launch {
                                                bottomSheetScaffoldState.bottomSheetState.hide()
                                            }
                                        }
                                    }
                                }
                            },
                            sheetContainerColor = Color.White,
                            containerColor = Color.Black,
                            sheetPeekHeight = if(mapState.isFollowingUser) 120.dp else 0.dp,
                        ){
                            MapBoxScreen(
                                currentPoint = point,
                                reports = mapState.reports,
                                routeLineData = routeLineStyle,
                                viewModel = viewModel,
                                context = context ,
                                mapViewportState = mapViewportState,
                                onMarkerClick = {reportId->
                                    scope.launch {
                                        viewModel.getReportById(reportId)
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                },
                                selectedLocation = mapState.selectedLocation
                            )
                        }
                    }
                }

                is PermissionStatus.Denied -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textToShow = if (locationPermissionsState.status.shouldShowRationale) {
                            "Ứng dụng cần quyền truy cập vị trí để hiển thị bản đồ. Vui lòng cấp quyền."
                        } else {
                            "Quyền truy cập vị trí bị từ chối. Vui lòng cấp quyền trong cài đặt."
                        }

                        Text(textToShow)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { locationPermissionsState.launchPermissionRequest() },

                            ) {
                            Text("Cấp quyền cho ứng dụng")
                        }
                    }
                }
            }
            AnimatedContainer(visible = !mapState.isFollowingUser) {
                Column(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)) {
                    SearchMapBoxScreen(
                        value = mapState.searchQuery,
                        onValueChange = {
                            viewModel.processIntent(MapIntent.UpdateQuery(it))
                           },
                        modifier = Modifier.padding(top = 25.dp),
                        focusManager = focusManager,
                        onClick = { navController.navigate("profile") },
                        onClear = {
                            viewModel.processIntent(MapIntent.ClearSearch)
                        }
                    )
                    AnimatedContainer(
                        visible = mapState.searchResults.isNotEmpty() && mapState.showSearchResult
                    ) {
                        SearchResults(mapState.searchResults,mapViewportState,focusManager){location->
                            viewModel.processIntent(MapIntent.SelectLocation(location))
                        }
                    }
                    AnimatedContainer(
                        //Khi ở chế độ tìm kiếm mà kết quả empty
                        visible =
                        mapState.searchResults.isEmpty() &&
                                mapState.searchQuery.isNotEmpty() &&
                                !mapState.isLoading &&
                                !mapState.isSearching &&
                                mapState.showSearchResult

                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(16.dp)//padding với màn hình
                                .border(1.dp, CustomColor, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp)) // Màu nền cho box
                                .padding(16.dp), // Padding bên trong box
                            contentAlignment = Alignment.Center // Căn giữa nội dung
                        ) {
                            Text(
                                text = "Không tìm thấy kết quả tìm kiếm",
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            // Location Card UI
            AnimatedContainer(visible = mapState.isFollowingUser) {
                LocationCard()
            }
        }

    }
// Tạo composable cho location card
@Composable
fun LocationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CustomColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.logo),
                contentDescription = "Location",
                tint = CustomColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Minh Hòa,Kinh Môn,Hải Dương",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily(Font(R.font.medium)),
                color = CustomColor,
                fontSize = 20.sp
            )
        }
    }
}
//Hiển thị kết quả tìm kiếm
@Composable
fun SearchResults(
    searchResults: List<SearchSuggestion>,
    mapViewportState: MapViewportState,
    focusManager:FocusManager,
    onLocationSelected: (MapMarker) -> Unit,
) {
    Column(
        modifier = Modifier.wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CustomColor, RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Text(
            text = "Kết quả tìm kiếm",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = CustomColor,
            textAlign = TextAlign.Start,
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.medium))
        )
        LazyColumn {
            items(searchResults) { suggestion ->
               Row (
                   modifier = Modifier
                       .fillMaxWidth()
                       .clickable {
                         suggestion.coordinate?.let {point->
                             mapViewportState.flyTo(
                                 cameraOptions = cameraOptions {
                                     center(point)
                                 },
                                 MapAnimationOptions.mapAnimationOptions {
                                     //Thời gian animation
                                     duration(1500)
                                 }
                             )
                         }
                           focusManager.clearFocus()
                           suggestion.fullAddress?.let { address->
                               onLocationSelected(MapMarker(suggestion.coordinate!!,address))
                           }
                       }
                       .padding(horizontal = 10.dp),
                   verticalAlignment = Alignment.CenterVertically
               ){
                   Icon(
                       imageVector = ImageVector.vectorResource(R.drawable.logo),
                       contentDescription = "Location",
                       tint = Color.Red,
                       modifier = Modifier.size(20.dp)
                   )
                   Text(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(16.dp),
                       text = "${suggestion.fullAddress}",
                   )
               }
            }
        }
    }

}

@Composable
private fun MapBoxScreen(
    currentPoint:Point,
    reports:List<Report>,
    onMarkerClick:(String)->Unit,
    routeLineData: Expected<RouteLineError, RouteSetValue>?,
    viewModel: MapViewModel,
    context:Context,
    mapViewportState:MapViewportState,
    selectedLocation: MapMarker? = null
) {

    val mapState by viewModel.mapState.collectAsStateWithLifecycle()
    val routeLine by viewModel.routeLine.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val lightPreset by viewModel.lightPreset.collectAsStateWithLifecycle()

    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = {
            NavigationStyle(
                routeLine = routeLine,
                progress = progress,
                lightPreset = lightPreset
            )
        }
    ){

        val routeLineView = remember { MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build()) }
        //Cập nhật bearing và indicator theo vị trí thực
        val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener  {
            mapViewportState.setCameraOptions{
                bearing(it)
            }
        }
        val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
            mapViewportState.setCameraOptions{
                center(it)
            }
            //Cập nhật progress
            if(it.hasAltitude()){
                viewModel._progress.value = it.altitude()
            }
        }
        val marker = rememberIconImage(
            key = "logo",
            painter = painterResource(id = R.drawable.location),
        )
        val dangerMarker = rememberIconImage(
            key = "danger",
            painter = painterResource(id =  R.drawable.danger),
        )
        //Hiển thị marker cho vị trí hiện tại
        PointAnnotation(
            point = currentPoint,
        ){
            iconImage = marker
            iconSize = 1.0
        }
        //Hiển thị marker cho vị trí tìm kiếm
        selectedLocation?.let {location->
            PointAnnotation(
                point = location.point,
            ){
                iconImage = marker
                iconSize = 1.0
            }
        }
        //Hiển thị danh sách marker
        reports.forEach { report->
            PointAnnotation(
                point = Point.fromLngLat(report.location.longitude,report.location.latitude),
                onClick = {
                    onMarkerClick(report.id)
                    true
                }
            ){
                iconImage = dangerMarker
                iconSize = 1.0
            }
        }
        MapEffect(Unit) {mapView->
            //Animation khi khởi động map
            mapViewportState.flyTo(
                cameraOptions = cameraOptions {
                    center(currentPoint)
                    zoom(15.0) // Zoom level mong muốn cho vị trí hiện tại
                    pitch(40.0) // Tùy chọn: thêm góc nghiêng
                },
                MapAnimationOptions.mapAnimationOptions {
                    //Thời gian animation
                    duration(5000)
                }
            )
            mapView.location.apply {
                addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            }
        }
        MapEffect(routeLine) {
            //Mỗi khi routeLine có sẵn camera sẽ chuyển về vị trí tổng quan
            routeLine?.let { lineString->
                mapViewportState.transitionToOverviewState(
                    OverviewViewportStateOptions.Builder()
                        .geometry(lineString)
                        .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                        .build()
                )
            }
        }

        MapEffect(routeLineData) {mapView->
            //vẽ route line
            routeLineData?.let { data ->
                mapView.mapboxMap.getStyle { style ->
                    Log.d("Style", "Style: $style")
                    routeLineView.renderRouteDrawData(style,data)
                }
            }
        }
        //Mỗi khi giá trị của isFollowing thay đổi thực hiện compose effect
        DisposableMapEffect(mapState.isFollowingUser){map->
            //Hiển thị icon chỉ đường và cập nhật camera của Map theo puck
            if(mapState.isFollowingUser){
                map.location.apply {
                    this.updateSettings {
                        //cấu hình icon chỉ đường
                        locationPuck = createDefault2DPuck(true)
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.HEADING
                        enabled= true
                    }
                    //Cập nhật route line với vị trí hiện tại
                    routeLine?.let {
                       // this.setLocationProvider(SimulateRouteLocationProvider(it))
                    }

                }
                //Camera sẽ đi theo vị trí của puck
                mapViewportState.transitionToFollowPuckState()

            }else{
                //Nếu không ở chế độ chỉ đường( turn by turn) sẽ quay về camera tổng quan chặng đường
                routeLine?.let { lineString->
                    mapViewportState.transitionToOverviewState(
                        OverviewViewportStateOptions.Builder()
                            .geometry(lineString)
                            .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                            .build()
                    )
                }
            }

            //Map sẽ lắng nghe sự thay đổi của vị trí hiện tại
            //map.location.addOnIndicatorPositionChangedListener(locationListener)
            onDispose {
                map.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                map.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            }
        }
    }
}

@Composable
fun MapBoxBottomSheet(
    modifier: Modifier = Modifier,
    currentLocation: Point,
    report: Report,
    viewModel: MapViewModel,
    onDismiss: () -> Unit,
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ){
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 10.dp),
            text = "Chi tiết báo cáo",
            style = MaterialTheme.typography.titleMedium,
            color = CustomColor,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.medium))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface (
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ){
                                AsyncImage(
                                    modifier = Modifier
                                        .clip(shape = CircleShape)
                                        .border(
                                            1.dp, Color.Gray,
                                            CircleShape
                                        ),
                                    model = report.userImage,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column{
                                Text(
                                    text = report.userName?:"Tên người dùng",
                                    fontWeight = FontWeight.Bold,
                                    color = CustomColor,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = report.timestamp.formatTimestamp(),
                                    fontFamily = FontFamily(Font(R.font.regular)),
                                    color = Color.Gray,
                                    fontSize = 14.sp,

                                    )
                                Text(
                                    text = report.location.latitude.toString() +
                                            "," + report.location.longitude.toString(),
                                    fontFamily = FontFamily(Font(R.font.regular)),
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )

                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Column() {
                            Text(
                                text = "Tình trạng : Đang sửa",
                                fontFamily = FontFamily(Font(R.font.regular)),
                                color = Color.Gray,
                                fontSize = 14.sp,

                                )
                            Text(
                                text = "Nội dung báo cáo :"+report.description,
                                fontFamily = FontFamily(Font(R.font.regular)),
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }


                }

                Box(
                    modifier = Modifier.size(120.dp)
                ){
                    if (report.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = report.imageUrl,
                            contentDescription = "Report Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
            }


    }
        Spacer(modifier = Modifier.height(16.dp))

        // Hai nút dưới cùng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val destination  = Point.fromLngLat(
                report.location.longitude,
                report.location.latitude
            )
            OutlinedButton(
                onClick = {

                    Log.d("MapboxScreen","Click: $destination,$currentLocation")
                    viewModel.processIntent(MapIntent.StartNavigation(currentLocation, destination))
                },
                border = BorderStroke(1.dp, CustomColor),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = CustomColor,
                    containerColor = Color.White
                )
                )
            {
                Icon(
                    painter = painterResource(R.drawable.arrow_sign),
                    contentDescription = "Arrow",
                    //tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Đường đi",
                    color = CustomColor,
                    fontFamily = FontFamily(Font(R.font.regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    if(!viewModel.mapState.value.isNavigating){
                        viewModel.processIntent(MapIntent.StartNavigation(currentLocation, destination))
                    }else{
                        viewModel.toggleCameraMode()
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = CustomColor),
            ) {
                Icon(
                    painter = painterResource(R.drawable.nav_icon),
                    contentDescription = "Navigation",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bắt đầu",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    viewModel.toggleCameraMode()

                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = CustomColor),
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo), // Đổi thành icon phù hợp
                    contentDescription = "Donate",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ủng hộ",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
}
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapBoxScreenPreview() {

}

@Composable
fun SearchMapBoxScreen(
    value:String,
    onValueChange:(String)->Unit,
    modifier: Modifier = Modifier,
    placeholder:String ="Tìm kiếm",
    focusManager:FocusManager,
    onClick:()->Unit,
    onClear:()->Unit
) {
    // Tạo một FocusRequester để kiểm soát focus
    val focusRequester = remember { FocusRequester() }
    // Tạo một FocusManager để quản lý focus

    // Tạo state để theo dõi trạng thái focus
    var isFocused by remember { mutableStateOf(false) }
           OutlinedTextField(
               value = value,
               onValueChange = onValueChange,
               placeholder = { Text(text = placeholder) },
               leadingIcon = {
                   if(isFocused){
                       IconButton(onClick = {
                           focusManager.clearFocus()
                           onClear()
                       }) {
                           Icon(
                               imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                               contentDescription = "Back"
                           )
                       }
                   }else{
                       Icon(
                           imageVector = ImageVector.vectorResource(id = R.drawable.logo),
                           contentDescription = "Search",
                           tint = Color.Red
                       )
                   }

               },
               colors = TextFieldDefaults.colors(
                   focusedContainerColor = Color.White,
                   unfocusedContainerColor = Color.White,
                   focusedTextColor = Color(0xFF007FFF)
               ),
               shape = RoundedCornerShape(24.dp),
               modifier = modifier
                   .fillMaxWidth()
                   .focusRequester(focusRequester)
                   .onFocusChanged {
                       isFocused = it.isFocused
                   }
                   .padding(10.dp)
                   .shadow(
                       elevation = 8.dp,
                       shape = RoundedCornerShape(24.dp),
                       ambientColor = DefaultShadowColor,
                       spotColor = DefaultShadowColor
                   )
               ,
               singleLine = true,
               trailingIcon = {
                  if(value.isNotEmpty()){
                          IconButton(
                              onClick = {
                                  onClear()
                                  focusManager.clearFocus()
                              }
                          ) {
                              Icon(
                                  imageVector = Icons.Default.Clear,
                                  contentDescription = "Clear"
                              )
                          }

                  }else{
                      Box(
                          modifier = Modifier
                              .size(35.dp)
                              .clip(CircleShape) // Định hình Box thành hình tròn
                              .border(2.dp, CustomColor, CircleShape)
                              .clickable { onClick() }
                      ) {
                          AsyncImage(
                              modifier = Modifier.fillMaxSize(),
                              model = Firebase.auth.currentUser?.photoUrl,
                              contentDescription = "Avatar",
                              contentScale = ContentScale.Fit
                          )
                      }
                  }
               },
               keyboardOptions = KeyboardOptions(
                   imeAction = ImeAction.Search
               )
           )
}
//Setup style cho map và route
@MapboxStyleComposable
@Composable
private fun NavigationStyle(
    routeLine: LineString?,
    progress: Double,
    lightPreset: LightPresetValue
) {
    val geoJsonSource = rememberGeoJsonSourceState {
        lineMetrics = BooleanValue(true)
    }

    LaunchedEffect(routeLine) {
        routeLine?.let {
            geoJsonSource.data = GeoJSONData(it)
        }
    }

    MapboxStandardStyle(
        topSlot = {
            if (routeLine != null) {
                LineLayer(
                    sourceState = geoJsonSource
                ) {
                    lineTrimOffset = DoubleListValue(listOf(0.0, progress))
                    lineWidth = DoubleValue(
                        interpolate {
                            exponential {
                                literal(1.5)
                            }
                            zoom()
                            stop {
                                literal(10)
                                product(7.0, 1.0)
                            }
                            stop {
                                literal(14.0)
                                product(10.5, 1.0)
                            }
                            stop {
                                literal(16.5)
                                product(15.5, 1.0)
                            }
                            stop {
                                literal(19.0)
                                product(24.0, 1.0)
                            }
                            stop {
                                literal(22.0)
                                product(29.0, 1.0)
                            }
                        }
                    )
                    lineCap = LineCapValue.ROUND
                    lineJoin = LineJoinValue.ROUND
                    lineGradient = ColorValue(
                        interpolate {
                            linear()
                            lineProgress()
                            stop {
                                literal(0)
                                rgba(47.0, 122.0, 198.0, 1.0)
                            }
                            stop {
                                literal(1.0)
                                rgba(47.0, 122.0, 198.0, 1.0)
                            }
                        }
                    )
                }
            }
        }
    ) {
        this.lightPreset = lightPreset
    }
}
//Hiện thống kê quãng đường
@Composable
fun TripProgressView(
    routeProgress:RouteProgress,
    modifier: Modifier = Modifier,
    context:Context,
    onPositioning:()->Unit,
    onDismiss:()->Unit
) {

       Row (
           modifier = modifier,
           horizontalArrangement = Arrangement.SpaceAround,
       ){
           IconButton(onClick = { onDismiss() },
               modifier = Modifier
                   .size(40.dp)
                   .border(1.dp, CustomColor, CircleShape)
           ) {
               Icon(
                   imageVector = Icons.Filled.Close,
                   contentDescription = "Back",
                   tint = CustomColor
               )
           }
           Column(horizontalAlignment = Alignment.CenterHorizontally) {
               Text(
                   text = routeProgress.formatDistance(context),
                   style = MaterialTheme.typography.titleMedium,
                   fontFamily = FontFamily(Font(R.font.heavy)),
                   fontSize = 20.sp,
                   color = CustomColor,
                   fontWeight = FontWeight.Bold
               )
               Text(
                   text = routeProgress.formatDuration(context),
                   style = MaterialTheme.typography.bodyMedium,
                   fontFamily = FontFamily(Font(R.font.regular)),
                   color = Color.LightGray
               )
           }
           IconButton(onClick = { onPositioning()},
               modifier = Modifier
                   .size(40.dp)
                   .border(1.dp, CustomColor, CircleShape)
           ) {
               Icon(
                   imageVector = ImageVector.vectorResource(R.drawable.ic_navigation_follow),
                   contentDescription = "Refresh",
                   tint = CustomColor
               )
           }
       }

}