package com.example.fixproblem.report.post.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner

//Intent - Các hành động từ người dùngg -> ViewModel
sealed class CameraIntent {
    data object Initialize : CameraIntent()
    data class CaptureImage(
        val context:Context
    ) : CameraIntent()
    data object HidePreview: CameraIntent()
    data object SwitchCamera: CameraIntent()
    data class SetupCamera(
        val context:Context,
        val lifecycleOwner: LifecycleOwner,
        val surfaceProvider: Preview.SurfaceProvider?
    ): CameraIntent()

    data object CleanupCamera: CameraIntent()
}