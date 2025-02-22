package com.example.fixproblem.presentation.ui.mapscreen

import com.example.fixproblem.data.model.remote.Report
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.search.result.SearchSuggestion


data class MapState (
    val isLoading: Boolean = false,
    val error: String? = null,
    val cameraCenter :Point? = null,
    val zoomLevel:Double = 15.0,
    val locations:List<GeoPoint> = emptyList(),
    val reports:List<Report> = emptyList(),
    val selectedReport: Report? = null,
    val isNavigating:Boolean = false,
    val navigationRoutes:NavigationRoute ? = null,
    val routeProgress:RouteProgress?= null,
    val routeLine: LineString? = null,
    val progress: Double = 0.0,
    val lightPreset: LightPresetValue = LightPresetValue.DAY,
    val isFollowingUser: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val selectedLocation: MapMarker? = null,
    val showSearchResult: Boolean = true
    )
//Todo:Chia nhỏ state đảm bảo separate of concerns
data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<SearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val selectedLocation: MapMarker? = null,
    val showSearchResult: Boolean = true
)
data class RouteState(
    val navigationRoutes:NavigationRoute ? = null,
    val routeProgress:RouteProgress?= null,
    val routeLine: LineString? = null,
    val progress: Double = 0.0,
    val lightPreset: LightPresetValue = LightPresetValue.DAY,
    val isFollowingUser: Boolean = false,
)
data class MapMarker (
    val point: Point,
    val address: String
)
