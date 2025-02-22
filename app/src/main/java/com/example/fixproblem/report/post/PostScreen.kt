package com.example.fixproblem.report.post

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fixproblem.presentation.theme.CustomColor
import com.example.fixproblem.Screen
import com.example.fixproblem.report.post.camera.CameraIntent
import com.example.fixproblem.report.post.camera.CameraSideEffect
import com.example.fixproblem.report.post.camera.CameraViewModel


@Composable
fun PostScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract =  ActivityResultContracts.RequestPermission(),
        onResult = { granted ->//khi chưa cấp quyền granted == false
            hasCameraPermission = granted
        }
    )
    LaunchedEffect(Unit) {
         if(!hasCameraPermission){
             launcher.launch(Manifest.permission.CAMERA)
         }
    }

    //Khi có quyền hiển thị camera
    if(hasCameraPermission){
        CameraScreen(navController = navController)
    }
}
@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CameraScreen(navController = NavController(LocalContext.current))
}
@Composable
fun CameraScreen(
    viewModel : CameraViewModel = hiltViewModel(),
    navController: NavController
    ){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewState by viewModel.cameraState.collectAsStateWithLifecycle()
    //Tạo preview view(tránh tạo lại view)
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    //Khi giá trị isFrontCamera thay đổi gọi lại hàm setupcamera
    LaunchedEffect(viewState.isFrontCamera) {
        viewModel.processIntent(
            CameraIntent.SetupCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.surfaceProvider
            )
        )
    }
    // Thiết lập camera khi component được tạo
    DisposableEffect(previewView) {
        viewModel.processIntent(
            CameraIntent.SetupCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.surfaceProvider
            )
        )
        // Clean up khi component bị hủy
        onDispose {
            viewModel.processIntent(CameraIntent.CleanupCamera)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect{effect->
            when(effect){
                is CameraSideEffect.ShowSuccess ->{
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                CameraSideEffect.RequestCameraPermission -> {
                    //Xử lý quyền trong view cha
                    //Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CameraSideEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.processIntent(CameraIntent.Initialize)
    }
    Box(
       modifier = Modifier.fillMaxSize()
    ){

        AndroidView(
            factory = {
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(
                visible = viewState.showPreview,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ImagePreview(
                    uri = viewState.lastCaptureUri,
                    onClose = {viewModel.processIntent(CameraIntent.HidePreview)},
                    onNavigate = {navController.navigate(
                        Screen.CreatePost.createRoute(Uri.encode(viewState.lastCaptureUri.toString()))
                    ){
                        popUpTo(Screen.Camera.route){
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }}
                )
            }
            CameraControls(
                modifier = Modifier.align(Alignment.BottomCenter),
                onSwitchCamera = {viewModel.processIntent(CameraIntent.SwitchCamera)},
                onCaptureImage = {viewModel.processIntent(CameraIntent.CaptureImage(context))}
            )

        if(viewState.isLoading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

//Setup camera:chuyển đổi camera, chup anh
@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    onSwitchCamera:()->Unit,
    onCaptureImage:()->Unit
) {
   Row(modifier = modifier
       .fillMaxWidth()
       .padding(16.dp),
       horizontalArrangement = Arrangement.SpaceEvenly)
   {
       IconButton(
           onClick = {
               onCaptureImage()
           }
       ) {
           Icon(imageVector = Icons.Default.Face,
               contentDescription = "Camera",
               tint = CustomColor,
           )
       }
       // Nút chuyển đổi camera
       IconButton (onClick = {
           onSwitchCamera()
       }
       ) {
           Icon(
               imageVector = Icons.Default.Refresh,
               contentDescription = "Đổi camera",
               tint = CustomColor
           )
       }
   }
}


@Composable
fun ImagePreview(
    uri:Uri?,
    onClose:()->Unit,
    onNavigate:()->Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Color.Black.copy(alpha = 0.8f)
        )
    ){
        uri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Ảnh vừa chụp",
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }
       IconButton(
           onClick = onClose,
           modifier = Modifier
               .align(Alignment.TopEnd)
               .padding(16.dp)
       ) {
           Icon(
               imageVector = Icons.Outlined.Close,
               contentDescription = "Đóng ảnh",
               tint = Color.White
           )
       }
        IconButton(
            onClick = onNavigate,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Thành công",
                tint = Color.White
            )
        }
    }
}

/*
@Composable
private fun CameraScreen(
    executor :Executor,
    lifecycleOwner:LifecycleOwner,
    context: Context
){
    //khởi tạo camera đằng sau
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    //=>Biến lensFacing để ghi nhớ trạng thaí camera hiện tại
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraProviderFuture = remember {ProcessCameraProvider.getInstance(context)}

    DisposableEffect(Unit) {
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, executor)
        onDispose {
            cameraProvider?.unbindAll()
        }
    }
    fun setupCamera(){

        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                //gỡ tất cả camera->đảm bảo không bị xung đột khi chuyển đổi
                provider.unbindAll()
                //Tạo mới lại các đối tượng
                preview = Preview.Builder().build()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                //Liên kết với camera mới
                provider.bindToLifecycle(
                    lifecycleOwner,
                    lensFacing,
                    preview,
                    imageCapture
                )
            }catch (e:Exception){
                Log.e("CameraX", "Setup failed", e)
            }
        },executor)
    }
    // Theo dõi thay đổi của lensFacing để cập nhật camera
    */
/*
    * Khi nhấn nút, giá trị của lensFacing thay đổi.
    Sử dụng LaunchedEffect(lensFacing),
*  mỗi khi lensFacing thay đổi, CameraX sẽ tự động gọi lại setupCamera().*//*

    LaunchedEffect(lensFacing) {
        setupCamera()
    }
    Column (
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ){
        AndroidView(
            factory = {context->
                val previewView= PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                cameraProviderFuture.addListener({
                    setupCamera()
                    preview?.setSurfaceProvider(previewView.surfaceProvider)
                },executor)
                previewView
            },
            update = {
                preview?.setSurfaceProvider(it.surfaceProvider)
            },
            onRelease = {
                //Giải phóng tài nguyên khi View bị hủy
                preview?.setSurfaceProvider(null)
                cameraProvider?.unbindAll()
                preview = null
                cameraProvider = null
            },
            modifier = Modifier.fillMaxSize().weight(1f)
        )
        Row (
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ){
            Button(
                onClick = {
                   imageCapture?.let {image->
                       captureImage(
                           context,
                           image,
                           executor
                       )
                   }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Chụp", color = Color(0xFF007FFF))
                Icon(imageVector = Icons.Filled.Face,
                    contentDescription = "Camera",
                    tint = Color(0xFF007FFF),
                    )
            }
            // Nút chuyển đổi camera
            Button(onClick = {
                //Khi nhấn nút đổi giá trị của lensFacing
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                Log.w("CameraX", "Switching camera to: ${if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) "back" else "front"}")
            },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                Text("Đổi camera", color = Color(0xFF007FFF))
            }
        }
    }

}

private fun captureImage(
    context:Context,
    imagePicture:ImageCapture,
    executor: Executor
){

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
            context.contentResolver,//Ghi vào bộ nhớ thiết bị
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//Luuư vào bộ nhớ ngoài
            contentValues//Truyền thông tin
        )
        .build()
    //Chụp ảnh và xử lý kết quả
    imagePicture.takePicture(
        outputOptions,
        executor,
        object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val msg = "Lưu ảnh thành công: ${outputFileResults.savedUri}"
                Log.d("CameraXApp", msg)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraXApp", "Lỗi", exception)
            }
        }
    )
}
*/
