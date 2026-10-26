package com.example.remedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.remedia.presentation.navigation.RemediaNavHost
import com.example.remedia.ui.theme.RemediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // El nombre del tema depende del nombre del proyecto (<Proyecto>Theme en ui/theme/Theme.kt)
            RemediaTheme {
                RemediaNavHost()
            }
        }
    }
}
