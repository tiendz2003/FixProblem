package com.example.fixproblem.presentation.ui.mapscreen

import com.mapbox.geojson.Point
import com.mapbox.navigation.base.route.NavigationRoute

// Intent/Events
sealed class MapIntent {
    data class LoadReportById(val id: String) : MapIntent()
    data class UpdateCurrentLocation(val location: Point) : MapIntent()
    data class StartNavigation(val origin: Point, val destination: Point) : MapIntent()
    data object StopNavigation : MapIntent()
    data class NavigationError(val error: String) : MapIntent()
    data class RouteUpdated(val route: NavigationRoute) : MapIntent()
    data class UpdateQuery(val query: String) : MapIntent()
    data object ClearSearch : MapIntent()
    data class SelectLocation(val location: MapMarker) : MapIntent()
}