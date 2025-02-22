package com.example.fixproblem.presentation.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fixproblem.R
import com.example.fixproblem.data.model.remote.Notification
import com.example.fixproblem.extension.formatTimestamp
import com.example.fixproblem.presentation.theme.CustomColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateToDetail:(String)->Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val topBarColor = remember {
        listOf(
            CustomColor,
            Color(0xFF60A5FA)
        )
    }
    LaunchedEffect(true) {
        viewModel.effect.collect{effect->
            when(effect){
                is NotificationEffect.OnNavigateToPost -> TODO()
                is NotificationEffect.ShowToast -> TODO()
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth().background(
                    brush =Brush.horizontalGradient(
                        colors = topBarColor
                    )
                ),
               colors = TopAppBarDefaults.topAppBarColors(
                   containerColor = Color.Transparent,
                   scrolledContainerColor = Color.White,
                   titleContentColor = Color.White,
                   actionIconContentColor = Color.White
                   ),
                title = {
                    Text(
                        modifier = Modifier.padding(20.dp),
                        text="Thông báo",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily(Font(R.font.heavy))
                    )
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) {padding->
        when{
            state.isLoading ->{
              CircularProgressIndicator()
            }
            state.error != null ->{
                Text(text = state.error?:"Lỗi khi tải thông báo", color = Color.Red)
            }
            state.notifications.isEmpty()->{
                Text(text = "Không có thông báo", color = Color.LightGray)
            }
            else ->{
                LazyColumn (
                    modifier = Modifier.fillMaxSize().background(Color.White).padding(padding)
                ){
                    items(state.notifications){notification->
                        NotificationItem(notification) {
                            //Xử lý onCLick

                            viewModel.processIntent(NotificationIntent.OnNotificationClick(notification))
                        }
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    //NotificationScreen()
}
@Composable
fun NotificationItem(
    notification: Notification,
    onClick:()->Unit
    ){
   // val isRead  by remember { mutableStateOf(notification.isRead) }
    val isRead = notification.isRead

       ConstraintLayout(
           modifier = Modifier.fillMaxSize().background(
               if(isRead) Color.White else Color(0xFFD6E9FF.toInt())
           )
       ) {
           val (reactionIcons,rowNotification,moreButton) = createRefs()
           val verticalGuildLine = createGuidelineFromBottom(0.5f)
           Box(
               modifier = Modifier
                   .constrainAs(reactionIcons){
                       // Đặt bottom của box ngang với top của avatar
                       bottom.linkTo(rowNotification.top, margin = 10.dp)
                       // Điều chỉnh vị trí ngang của box
                       start.linkTo(rowNotification.start, margin = 30.dp)
                   }
                   .size(20.dp)
                   .clip(CircleShape)
                   .background(Color.Red)
           ) {
               Icon(
                   imageVector = Icons.Default.Favorite,
                   contentDescription = "Emotion Icon",
                   modifier = Modifier
                       .size(16.dp)
                       .align(Alignment.Center),
                   tint = Color.White
               )
           }

           Row(
               modifier = Modifier
                   .constrainAs(rowNotification){
                       top.linkTo(parent.top)
                       start.linkTo(parent.start)
                       end.linkTo(parent.end)
                   }
                   .fillMaxWidth()
                   .clickable(onClick = onClick)
                   .padding(5.dp),
               verticalAlignment = Alignment.CenterVertically,
           ) {

               //Avatar
               AsyncImage(
                   model = notification.userImg,
                   contentDescription = "Avatar",
                   modifier = Modifier.size(40.dp).clip(CircleShape),
                   contentScale = ContentScale.Crop
               )

               Spacer(modifier = Modifier.width(8.dp))
               //Thông báo
               Column(
                   modifier = Modifier.weight(1f)
               ) {
                   Text(
                       text=notification.message,
                       maxLines = 3,
                       fontSize = 16.sp
                   )
                   Row {
                       Text(
                           text = notification.timestamp.formatTimestamp(),
                           fontSize = 14.sp,
                           color = CustomColor
                       )
                       Spacer(modifier = Modifier.width(2.dp))
                       Text(
                           text = "1 cảm xúc",
                           fontSize = 14.sp,
                           color = CustomColor
                       )
                       Spacer(modifier = Modifier.width(2.dp))
                       Text(
                           text = " 11 bình luận",
                           fontSize = 14.sp,
                           color = CustomColor
                       )

                   }

               }
           }
           IconButton(
               onClick = {},
               modifier = Modifier
                   .constrainAs(moreButton){
                       top.linkTo(parent.top)
                       start.linkTo(rowNotification.end)
                       end.linkTo(parent.end)
                   }.padding(end =15.dp)
           ) {
               Icon(
                   imageVector = Icons.Filled.MoreVert,
                   contentDescription = "Tùy chọn",
                   tint = Color.LightGray.copy(alpha = 0.5f)
               )
           }
       }
}