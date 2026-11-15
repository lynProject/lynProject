package com.example.lynproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lynproject.ui.navigation.AppNavigation
import com.example.lynproject.ui.theme.LynProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LynProjectTheme {
                AppNavigation()
            }
        }
    }
}
