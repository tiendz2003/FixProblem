package com.example.fixproblem.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fixproblem.presentation.theme.FixProblemTheme
import com.example.fixproblem.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kiểm tra người dùng đã đăng nhập chưa
        Firebase.auth.currentUser?.let { user ->
            if (!user.email.isNullOrBlank()) {
                // Nếu đã đăng nhập thì chuyển ngay sang MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
        }
        enableEdgeToEdge()
        setContent {
            FixProblemTheme(
                darkTheme = false
            ) {
                LoginScreen(
                    onNavigateToHome = {
                        if(Firebase.auth.currentUser == null) return@LoginScreen else{
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}