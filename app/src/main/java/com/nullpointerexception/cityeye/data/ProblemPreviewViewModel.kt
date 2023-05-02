package com.nullpointerexception.cityeye.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import com.nullpointerexception.cityeye.util.LocationUtil
import kotlinx.coroutines.launch
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

    fun setAddress(address: String?) {
        _address.value = address
    }

    private val _response = MutableLiveData<Boolean>()
    val response: MutableLiveData<Boolean>
        get() = _response

    fun setResponse(response: Boolean) {
        _response.value = response
    }

    fun addProblem(
        context: Context,
        title: String,
        description: String,
        savedImageFile: File,
        location: LatLng,
        address: String
    ) {
        viewModelScope.launch {
            val response = FirebaseDatabase.addNormalProblem(
                context,
                title,
                description,
                savedImageFile,
                location,
                address
            )
            setResponse(response)
        }
    }

    fun getAddressFromLocation(context: Context, latLng: LatLng) {
        viewModelScope.launch {
            val address = LocationUtil.getAddressFromCo(context, latLng)
            setAddress(address)
        }
    }

}