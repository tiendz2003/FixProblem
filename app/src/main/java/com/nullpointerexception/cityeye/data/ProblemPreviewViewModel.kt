package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import java.io.File

class ProblemPreviewViewModel : ViewModel() {

    private val _coordinates = MutableLiveData<LatLng?>()
    val coordinates: MutableLiveData<LatLng?>
        get() = _coordinates

    private val _image = MutableLiveData<File>()
    val image: LiveData<File>
        get() = _image

    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    fun setCoordinates(latLng: LatLng?) {
        _coordinates.value = latLng
    }

    fun setImage(image: File) {
        _image.value = image
    }

    fun setAddress(address: String) {
        _address.value = address
    }

}