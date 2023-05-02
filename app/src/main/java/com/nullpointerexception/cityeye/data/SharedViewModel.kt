package com.nullpointerexception.cityeye.data

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.model.PlaceType
import com.google.maps.model.PlacesSearchResponse
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.entities.SupportedCities
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.maps.model.LatLng as NewLatLng


class SharedViewModel : ViewModel() {

    private val _myCoordinates = MutableLiveData<LatLng>()
    var myCoordinates: LiveData<LatLng> = _myCoordinates

    fun setMyCoordinates(newCoordinates: LatLng) {
        _myCoordinates.value = newCoordinates
    }

    fun getMyCoordinates(): MutableLiveData<LatLng> {
        return _myCoordinates
    }

    private val _problemCoordinates = MutableLiveData<List<LatLng>>()
    var problemCoordinates: LiveData<List<LatLng>> = _problemCoordinates

    fun setCoordinates(newCoordinates: List<LatLng>) {
        _problemCoordinates.value = newCoordinates
    }

    fun getCoordinates(): MutableLiveData<List<LatLng>> {
        return _problemCoordinates
    }

    var isLoaded = false

    fun loadProblemCoordinates() {
        viewModelScope.launch {
            val response = FirebaseDatabase.getAllProblems()
            val coordinates: MutableList<LatLng> = mutableListOf()

            for (problem in response) {
                problem.location_lat?.toDouble()?.let {
                    problem.location_lon?.toDouble()?.let { it1 ->
                        LatLng(
                            it,
                            it1
                        )
                    }
                }?.let {
                    coordinates.add(
                        it
                    )
                }
            }

            setCoordinates(
                coordinates
            )
        }
    }


    private val _supportedCities = MutableLiveData<SupportedCities>()
    var supportedCities: LiveData<SupportedCities> = _supportedCities

    fun getSupportedCities(): MutableLiveData<SupportedCities> {
        return _supportedCities
    }

    fun setSupportedCities(sc: SupportedCities) {
        _supportedCities.value = sc
    }

    fun getlatestSupportedCities() {
        viewModelScope.launch {
            val response = FirebaseDatabase.getSupportedCities()
            response?.let { setSupportedCities(it) }

        }
    }

    private val _places = MutableLiveData<PlacesSearchResponse>()

    fun getPlaces(): MutableLiveData<PlacesSearchResponse> {
        return _places
    }

    fun setPlaces(sc: PlacesSearchResponse) {
        _places.value = sc
    }

    @SuppressLint("MissingPermission")
    fun getNearbyPlaces(activity: Activity, myLocation: LatLng?, met: Int) {

        viewModelScope.launch {
            var request: PlacesSearchResponse?
            var fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
            Places.initialize(
                activity.applicationContext,
                activity.applicationContext.getString(R.string.maps_key)
            )

            val location = myLocation ?: withContext(Dispatchers.IO) {
                fusedLocationClient.lastLocation.await()
            }

            val apiContext: GeoApiContext = GeoApiContext.Builder()
                .apiKey(activity.applicationContext.getString(R.string.maps_key))
                .build()

            request = withContext(Dispatchers.IO) {

                var nLocation: LatLng? = if (location is Location) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    location as LatLng
                }

                PlacesApi.nearbySearchQuery(
                    apiContext,
                    NewLatLng(nLocation?.latitude!!, nLocation.longitude)
                )
                    .radius(met)
                    .type(PlaceType.CAFE)
                    .language("en")
                    .await()
            }

            setPlaces(request)

        }
    }


}