package com.example.fixproblem.report.post.camera

import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CameraViewModel @Inject constructor():ViewModel() {
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState = _cameraState.asStateFlow()

    private val _sideEffect = Channel<CameraSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun processIntent(intent: CameraIntent){
        when(intent){
            is CameraIntent.Initialize -> initialize()
            is CameraIntent.SwitchCamera -> switchCamera()
            is CameraIntent.CaptureImage -> captureImage(intent)
            is CameraIntent.HidePreview -> showPreview()
            is CameraIntent.SetupCamera -> setupCamera(intent)
            is CameraIntent.CleanupCamera -> cleanupCamera()
        }
    }

    private fun setupCamera(intent: CameraIntent.SetupCamera) {
        val cameraSelector = if(_cameraState.value.isFrontCamera){
            CameraSelector.DEFAULT_FRONT_CAMERA
        }else{
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        //Khởi tạo camera
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(intent.context)
        cameraProviderFuture.addListener({
           try {
               if(cameraProvider == null){
                   cameraProvider = cameraProviderFuture.get()
               }else{
                   cameraProvider?.unbindAll()
               }
               // Thiết lập preview
               preview = Preview.Builder().build().also {
                   it.setSurfaceProvider(intent.surfaceProvider)
               }
               // Thiết lập image capture
               imageCapture = ImageCapture.Builder()
                   .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                   .build()

               // Bind các use case với lifecycle
               cameraProvider?.bindToLifecycle(
                   intent.lifecycleOwner,
                   cameraSelector,
                   preview,
                   imageCapture
               )
               // Cập nhật trạng thái
               _cameraState.update { it.copy(
                   isLoading = false,
                   error = null
               ) }
           }catch (e:Exception){
               _cameraState.update { it.copy(isLoading = false,error = e.message) }
               viewModelScope.launch {
                   _sideEffect.send(CameraSideEffect.ShowError(e.message ?: "Cài đặt camera lỗi"))
               }
           }

        }, ContextCompat.getMainExecutor(intent.context))
    }
    private fun captureImage(intent: CameraIntent.CaptureImage) {
        val imageCapture = this.imageCapture?:return
        val contentValues = ContentValues().apply {
            //Tên tệp
            put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${Random.nextInt()}.jpg")
            //Loại file ảnh
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            //Đường dẫn thư mục
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                intent.context.contentResolver,//Ghi vào bộ nhớ thiết bị
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//Luuư vào bộ nhớ ngoài
                contentValues//Truyền thông tin
            ).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,//Chạy trên 1 executor khác
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    //Lưu ảnh uri -> lastCaptureUri

                    outputFileResults.savedUri?.let { uri ->
                        viewModelScope.launch(Dispatchers.Main) {
                        _cameraState.update {
                            it.copy(
                                showPreview = true,
                                lastCaptureUri = uri
                            )
                        }
                            _sideEffect.send(CameraSideEffect.ShowSuccess("Chụp ảnh thành công"))
                    }
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    viewModelScope.launch {
                        _sideEffect.send(
                            CameraSideEffect.ShowError(
                                exception.message ?: "Chụp ảnh lỗi"
                            )
                        )
                    }
                }
            }
        )

    }
    private fun showPreview() {
        _cameraState.update { it.copy(showPreview = false) }
    }
    private fun switchCamera() {
        _cameraState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }
    private fun initialize() {
       _cameraState.update { it.copy(isLoading = true) }
    }
    //Tài nguyên máy -> Giải phóng tài nguyên khi thoát khỏi màn hình camera
    private fun cleanupCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            preview = null
            imageCapture = null
        } catch (e: Exception) {
            viewModelScope.launch {
                _sideEffect.send(CameraSideEffect.ShowError("Giải phóng không thành công: ${e.message}"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}