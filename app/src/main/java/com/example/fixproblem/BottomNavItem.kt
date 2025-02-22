package com.example.fixproblem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen (
    val route: String,
    val icon:ImageVector?=null
){
    data object Map : Screen("Bản đồ",Icons.Filled.LocationOn)
    data object List : Screen("Bài viết",Icons.Filled.Favorite)
    data object Post : Screen("Đăng",Icons.Filled.Add)
    data object Notification : Screen("Thông báo",Icons.Filled.Notifications)
    data object Setting : Screen("Cài đặt",Icons.Filled.Settings)
    data object Camera: Screen("Đăng",Icons.Filled.Add)
    data object CreatePost : Screen("create_post?imageUri={imageUri}") {
        fun createRoute(imageUri: String?) = "create_post?imageUri=$imageUri"
    }
}
