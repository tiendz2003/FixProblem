package com.example.fixproblem

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fixproblem.fcm.FCMManager
import com.example.fixproblem.presentation.theme.CustomColor
import com.example.fixproblem.presentation.theme.FixProblemTheme
import com.example.fixproblem.presentation.ui.mapscreen.HomeScreen
import com.example.fixproblem.presentation.ui.mapscreen.MapViewModel
import com.example.fixproblem.presentation.ui.notification.NotificationScreen
import com.example.fixproblem.report.post.PostScreen
import com.example.fixproblem.report.newsfeed.NewsFeedScreen
import com.example.fixproblem.report.post.CreatePostScreen
import com.example.fixproblem.presentation.ui.profile.ProfileScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var fcmManager: FCMManager
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM_DEBUG", "Notification permission granted")
        } else {
            Log.d("FCM_DEBUG", "Notification permission denied")
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //toàn màn hình
       // MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        checkNotificationPermission()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_DEBUG", "Current FCM token: $token")
                    // Cập nhật token
                    CoroutineScope(Dispatchers.IO).launch {
                        fcmManager.updateToken(token)
                    }
                } else {
                    Log.e("FCM_DEBUG", "Failed to get FCM token", task.exception)
                }
            }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContent {
            val navController = rememberNavController()
            val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior(
                snapAnimationSpec = tween(durationMillis = 300),
            )
            val showBottomBar by remember { mutableStateOf(true) }
            // Animate BottomNav
            val bottomNavOffset by animateFloatAsState(
                targetValue = if (showBottomBar) 0f else 100f,
                animationSpec = tween(300), label = ""
            )//Hiệu ứng để ẩn bottom bar
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            //ẩn bottom bar ở 1 màn hình cụ thể
            val (camera,post,profile) =
                Triple(Screen.Camera.route, Screen.CreatePost.route,"profile")
            val isShowBottomBar = remember(navController.currentBackStackEntry ) {
                when {
                    navBackStackEntry.value?.destination?.route in listOf(camera, post, profile) -> false
                    else -> true
                }
            }

            FixProblemTheme(darkTheme = false) {
                val systemUiController = rememberSystemUiController()
                //Ẩn thanh trạng thái
                SideEffect {
                    systemUiController.isSystemBarsVisible = false
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ){
                    //systemUiController.isSystemBarsVisible = false
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize(),
                        bottomBar = {
                            if(isShowBottomBar){
                                BottomAppBar(
                                    modifier = Modifier.fillMaxWidth().offset{
                                        IntOffset(y = bottomNavOffset.toInt(), x = 0)
                                    } ,// Add animation of
                                    scrollBehavior = scrollBehavior,
                                    containerColor = CustomColor
                                ) {
                                    MainBottomNavigation(navController = navController)
                                }
                            }
                        },
                    ) { innerPadding ->
                        NavigationGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),

                        )
                    }
                }
            }
        }
    }
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,

) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.List.route) {
            NewsFeedScreen(navController)
        }
        composable(
            Screen.Post.route,
            enterTransition = { slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) },
        ) {

            CreatePostScreen(
                navController = navController,
                imgUri = null
            )
        }
        composable(
            route = Screen.CreatePost.route,
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            CreatePostScreen(
                navController = navController,
                imgUri = imageUri?.let { Uri.parse(it) }
            )
        }
        composable(
            route = Screen.Camera.route,
            enterTransition = { slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(600)
            ) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) },
            ) {
            PostScreen(navController = navController)

        }
        composable("profile") {
            ProfileScreen()
        }
        composable(Screen.Notification.route) {
            NotificationScreen{
                //todo
            }
        }
        composable(Screen.Setting.route) {
            //SettingsScreen()
        }

    }
}
@Composable
fun MainBottomNavigation(navController:NavController  ) {
    val backgroundShape = remember { menuBarShape() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route
    val navItem = listOf(
        Screen.Map,
        Screen.List,
        Screen.Camera,
        Screen.Notification,
        Screen.Setting,
    )
   Row(
       modifier = Modifier.fillMaxWidth(),
       horizontalArrangement = Arrangement.SpaceEvenly
   ) {
        navItem.forEach { navItem ->
            NavigationBarItem(
                icon = { Icon(imageVector = navItem.icon!!,
                    contentDescription = navItem.route,
                    tint = if(currentRoute == navItem.route) Color.Blue else Color.White) },
                label = { Text(navItem.route, color = Color.White, fontFamily = FontFamily(Font(R.font.regular))) },
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route){
                        popUpTo(navController.graph.startDestinationId){
                            //Lưu trạng thái
                            saveState = true
                        }
                        //Tránh tạo bản sao nếu cùng 1 điểm đến
                        launchSingleTop = true
                        //Phục hồi trạng thái khi quay trở lại
                        restoreState = true
                    }
                }
            )
        }
    }

}

private fun menuBarShape() = GenericShape{size,_->
reset()
moveTo(0f,0f)
val width = 150f
val height = 90f
val point1 = 75f
val point2 = 85f
lineTo(size.width/2-width,0f)
cubicTo(
    size.width / 2 - point1, 0f,
    size.width / 2 - point2, height,
    size.width / 2, height
)
cubicTo(
    size.width/2+point2,height,
    size.width/2+point1,0f,
    size.width/2+width,0f,
)
/*lineTo(size.width / 2 + width, 0f)*/
lineTo(size.width,0f)
lineTo(size.width,size.height)
lineTo(0f,size.height)
close()
}

