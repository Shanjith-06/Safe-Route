package com.example.saferoute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.saferoute.ui.dashboard.MainDashboardScreen
import com.example.saferoute.ui.theme.SafeRouteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeRouteTheme {
                MainDashboardScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    SafeRouteTheme {
        MainDashboardScreen()
    }
}
