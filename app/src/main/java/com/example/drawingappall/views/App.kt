package com.example.drawingappall.views

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
    // allows us to switch between the multiple screens
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .background(Color(0xFFEEEEEE)) // gray background
    ) {
        // holds the screens and does android stuff behind the scenes
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->

            // NavHost manages which screen is currently visible
            NavHost(
                navController = navController,
                startDestination = "splash", // start at the splash screen
                modifier = Modifier.padding(paddingValues)
            ) {

                // Splash screen route
                composable("splash") {
                    // On finish, navigate to gallery screen
                    SplashScreen { navController.navigate("gallery") }
                }

                // Drawing screen route, with dynamic filepath and filename
                composable("draw/{filepath}/{filename}") { backStackEntry ->
                    val filePath = backStackEntry.arguments?.getString("filepath").orEmpty()
                    val fileName = backStackEntry.arguments?.getString("filename").orEmpty()

                    DrawScreen(
                        navController = navController,
                        filePath = filePath,
                        fileName = fileName
                    )
                }

                // Gallery screen route
                composable("gallery") {
                    GalleryScreen(navController = navController)
                }
            }
        }
    }
}