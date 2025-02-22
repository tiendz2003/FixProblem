package com.example.fixproblem.report.post


import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fixproblem.R
import com.example.fixproblem.presentation.theme.CustomColor
import com.example.fixproblem.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController? = null,
    viewModel: PostCreationViewModel = hiltViewModel(),
    imgUri: Uri?
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior =TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(
        state.isSuccess
    ) {
        if(state.isSuccess){
            Toast.makeText(context, "Đăng bài viết thành công!!!", Toast.LENGTH_SHORT).show()
            navController?.popBackStack()
        }
    }
    LaunchedEffect(
        state.error
    ) {
        state.error?.let {
            Toast.makeText(context,"Lỗi: $it" , Toast.LENGTH_SHORT).show()
        }
    }
    //LẤY URI
    LaunchedEffect(imgUri) {
      imgUri?.let {
          viewModel.onEvent(PostCreationEvent.UpdateImage(it))
      }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background).padding(16.dp),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(bottom = 30),
                title = { Text(
                    text = "Bài đăng mới",
                    fontFamily = FontFamily(Font(R.font.medium)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController?.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(PostCreationEvent.SubmitPost)
                        },
                        enabled = state.caption.isNotEmpty()||state.imageUri != null,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(text="Chia sẻ",
                            fontFamily = FontFamily(Font(R.font.regular)),
                            color = if(state.caption.isNotEmpty())
                            Color(0xFF007FFF) else Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding->
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ){
            when{
                state.isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                state.imageUri != null -> {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Ảnh bài viết",
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                }else->{
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, CustomColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            navController?.navigate(Screen.Camera.route)
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(48.dp),
                                tint = CustomColor
                            )
                        }
                    }
                }


            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                AsyncImage(
                   model =viewModel.firebaseAuth.currentUser?.photoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = viewModel.firebaseAuth.currentUser?.displayName?:"Tên người dùng",
                        fontFamily = FontFamily(Font(R.font.medium)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis())),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily(Font(R.font.regular))
                    )
                    Text(
                        text = state.address.ifEmpty { "Đang lấy vị trí..." },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily(Font(R.font.regular))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            //Caption
            OutlinedTextField(
                value = state.caption,
                onValueChange = {
                    viewModel.onEvent(PostCreationEvent.UpdateCaption(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Viết cảm nghĩ của bạn")},
                maxLines = 5
            )
            Spacer(Modifier.height(16.dp))
            Column {
                ListItem(
                    headlineContent = { Text(text = "Thêm vị trí") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location"
                        )
                    },
                    modifier = Modifier.clickable {
                        //todo:chuyển tới màn hình Map
                    }
                )
                ListItem(
                    headlineContent = { Text(text = "Thư viện ảnh") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Add Image"
                        )
                    },
                    modifier = Modifier.clickable {
                        //todo: chuyển tới màn hình thư viện ảnh
                    }
                )
            }
        }
    }

}



/*
@Composable
fun PostCreationScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPostClick:()->Unit
) {
    val dateTime = SimpleDateFormat("HH:mm dd/MM/yyyy",Locale.getDefault())
        .format(Date())
    var postText by remember { mutableStateOf("") }
    val currentTime = remember { dateTime }
    val location = "Bắc Từ Liêm,Hà Nội"
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp)

    ) {
        //Ảnh người dùng
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Avatar",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text = "Người dùng", style = MaterialTheme.typography.titleMedium)
                Text(text = currentTime, style = MaterialTheme.typography.bodySmall)
                Text(text = location, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            //Vùng nhập text
            TextField(
                value = postText,
                onValueChange = { postText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Cảm nghĩ của bạn về vấn đề này?")},
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(Modifier.height(16.dp))
            //Hình ảnh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center

            ) {
                //PlaceHolder
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        ){
                        Text(
                            text = "Chọn hình ảnh",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily(Font(R.font.regular))
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                //Các nút chức năng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SetupIconButton(
                        onCameraClick,
                        imageVector = Icons.Outlined.Add,
                        content = "Camera",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    //Nút thư viện ảnh
                    SetupIconButton(
                        onGalleryClick,
                        imageVector = Icons.Outlined.Face,
                        content = "Thu viện ảnh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    //Nút đăng bài
                    SetupIconButton(
                        onPostClick,
                        imageVector = Icons.Filled.Send,
                        content = "Đăng bài",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun SetupIconButton(
    onClick:()->Unit,
    imageVector:ImageVector,
    content:String,
    tint:Color,
    modifier: Modifier = Modifier
){
    IconButton(
        onClick = onClick
    ){
        Icon(
            imageVector = imageVector,
            contentDescription = content,
            tint = tint
        )
    }
}*/
