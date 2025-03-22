package com.example.drawingappall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun App() {
    // allows us to switch between the two screens
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .background(Color(0xFFEEEEEE)) // gray background
    ) {
        // holds the screens and does android stuff behind the scenes
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "splash", // start at the splash screen
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("splash") {
                    SplashScreen { navController.navigate("gallery") } // navigate to the draw screen
                }
                composable("draw") {
                    DrawScreen(navController = navController)
                }
                composable("gallery") {
                    GalleryScreen(navController = navController)
                }
            }
        }
    }
}