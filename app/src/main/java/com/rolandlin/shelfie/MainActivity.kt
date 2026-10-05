package com.rolandlin.shelfie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rolandlin.shelfie.core.designsystem.theme.ShelfieTheme
import com.rolandlin.shelfie.navigation.ShelfieNavDisplay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfieTheme {
                Surface(Modifier.fillMaxSize()) {
                    ShelfieNavDisplay()
                }
            }
        }
    }
}
