package com.dogusipeksac.notification_programming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dogusipeksac.notification_programming.ui.navigation.AppNavGraph
import com.dogusipeksac.notification_programming.ui.theme.Notification_programmingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Notification_programmingTheme {
                AppNavGraph()
            }
        }
    }
}
