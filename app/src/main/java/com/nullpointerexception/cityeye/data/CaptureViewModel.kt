package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class CaptureViewModel : ViewModel() {

    private val _coordinates = MutableLiveData<LatLng>()
    var coordinates: LiveData<LatLng> = _coordinates

    fun setCoordinates(newCoordinates: LatLng) {
        _coordinates.value = newCoordinates
    }

    fun getCoordinates(): MutableLiveData<LatLng> {
        return _coordinates
    }

}