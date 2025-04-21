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

/**
 * Root composable that sets up navigation and top-level layout.
 */
@Composable
fun App() {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .background(Color(0xFFEEEEEE)) // light gray background
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues)
            ) {

                composable("splash") {
                    SplashScreen(
                        onNavigateToGallery = {
                            navController.navigate("gallery") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("draw/{filepath}/{filename}") { backStackEntry ->
                    val filePath = backStackEntry.arguments?.getString("filepath").orEmpty()
                    val fileName = backStackEntry.arguments?.getString("filename").orEmpty()

                    DrawScreen(
                        navController = navController,
                        filePath = filePath,
                        fileName = fileName
                    )
                }

                composable("gallery") {
                    GalleryScreen(navController = navController)
                }
            }
        }
    }
}
