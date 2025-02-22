package com.example.fixproblem.report.post.camera

import android.net.Uri

//Trạng thái của UI
data class CameraState(
    val isLoading: Boolean = true,
    val error:String?= null,
    val isFrontCamera: Boolean = false,
    val showPreview: Boolean = false,
    val lastCaptureUri:Uri? = null,
    val hasCameraPermission:Boolean = false
)
//
sealed class CameraSideEffect{
    data class ShowError(val message: String) : CameraSideEffect()
    data class ShowSuccess(val message: String) : CameraSideEffect()
    data object RequestCameraPermission : CameraSideEffect()
}