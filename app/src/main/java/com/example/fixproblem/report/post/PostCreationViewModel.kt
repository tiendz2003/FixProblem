package com.example.fixproblem.report.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixproblem.data.model.remote.Report
import com.example.fixproblem.data.repository.MapBoxRepository
import com.example.fixproblem.data.repository.ReportRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostCreationViewModel @Inject constructor (
    val firebaseAuth: FirebaseAuth,
    private val reportRepository: ReportRepository,
    private val mapRepository:MapBoxRepository,

):ViewModel() {
    private val _uiState = MutableStateFlow(PostCreationState())
    val uiState get() = _uiState.asStateFlow()
    private fun updateState(update:(PostCreationState)->PostCreationState) = _uiState.update(update)

    fun onEvent(event:PostCreationEvent){
        when(event){
            is PostCreationEvent.SubmitPost -> submitPost()
            is PostCreationEvent.UpdateCaption -> _uiState.update { it.copy(caption = event.caption) }
            is PostCreationEvent.UpdateImage -> _uiState.update { it.copy(imageUri = event.imgUri) }
            is PostCreationEvent.UpdateLocation -> _uiState.update { it.copy(location = event.location) }
        }
    }
    init {
        updateLocation()
        viewModelScope.launch {
            mapRepository.location.collect { point ->
                point?.let { location ->
                    // Chuyển đổi Point sang GeoPoint
                    val geoPoint = GeoPoint(
                        location.latitude(),
                        location.longitude()
                    )

                    Log.d("Location", "Current location: $geoPoint")
                    updateState { it.copy(location = geoPoint) }

                }
            }
        }
    }
    private fun updateLocation(){
        viewModelScope.launch {
            try {
                mapRepository.getCurrentLocation()
                mapRepository.location.collectLatest { point->
                    point?.let { location ->
                        Log.d("Location", "Current location: ${uiState.value.location}")
                        mapRepository.getAddressFromCoordinates(
                            latitude = location.latitude(),
                            longitude = location.longitude()
                        ).collectLatest {result->
                            result.onSuccess {
                                address-> updateState { it.copy(address = address) }
                            }.onFailure {error->
                                updateState { it.copy(address ="Không tìm thấy địa chỉ${error.message}" ) }
                            }
                        }
                    }

                }
            }catch (e:Exception){
                updateState { it.copy(address = "Không tìm thấy địa chỉ") }
                Log.d("Location", e.message.toString())
            }
        }
    }
    private fun submitPost() {
       viewModelScope.launch {
           updateState {
               it.copy(isLoading = true, error = null)
           }
           try {
               val currentUser = firebaseAuth.currentUser
               //Kiểm tra người dùng đã đăng nhập chưa
               if(currentUser == null){
                   updateState {
                       it.copy(error = "Người dùng chưa đăng nhập")
                   }
                   return@launch
               }

               //Tải ảnh nên firebase storage ->lấy url -> tạo bài viết kèm url
               val imageUrl = uiState.value.imageUri?.let {img->
                   reportRepository.uploadImage(imageUri = img, onProgress = {progress->
                       updateState { it.copy(uploadProgress = progress) }
                   })
               }?:""
               // Kiểm tra location
               val currentLocation = uiState.value.location
               Log.d("submitPost", "Current location: $currentLocation")
               if (currentLocation == null) {
                   updateState { it.copy(error = "Không tìm được vị trí, vui lòng thử lại") }
                   return@launch
               }
               val report = Report(
                   id = UUID.randomUUID().toString(),
                   userName = currentUser.displayName,
                   userImage =currentUser.photoUrl.toString(),
                   title = uiState.value.caption,
                   description = uiState.value.caption,
                   imageUrl = imageUrl,
                   location = GeoPoint(currentLocation.latitude, currentLocation.longitude),
                   timestamp = System.currentTimeMillis(),
                   userId = currentUser.uid,
               )
               reportRepository.createReport(report)
               updateState {
                   it.copy(isLoading = false, isSuccess = true, uploadProgress = 1f)
               }
           }catch (e:Exception){
               updateState {
                   it.copy(isLoading = false, error = "Lỗi khi tạo bài viết ${ e.message }")
               }
           }
       }
    }

}