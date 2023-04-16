package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class CaptureViewModel : ViewModel() {

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

    fun loadProblemCoordinates() {
        viewModelScope.launch {
            val response = FirebaseDatabase.getAllProblems()
            val coordinates: MutableList<LatLng> = mutableListOf()

            for (problem in response) {
                coordinates.add(
                    LatLng(
                        problem.location_lat!!.toDouble(),
                        problem.location_lon!!.toDouble()
                    )
                )
            }

            setCoordinates(
                coordinates
            )
        }
    }


}