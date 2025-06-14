package com.gibraltar0123.traveldiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gibraltar0123.traveldiary.ui.screen.MainScreen
import com.gibraltar0123.traveldiary.ui.theme.TravelDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelDiaryTheme {
                MainScreen()
            }
        }
    }
}