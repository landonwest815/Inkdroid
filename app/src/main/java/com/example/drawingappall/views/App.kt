package com.example.drawingappall.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawingappall.viewModels.DrawingFileViewModel
import com.example.drawingappall.viewModels.DrawingViewModelProvider

/**
 * Root composable: sets up app navigation and layout.
 */
@Composable
fun App() {
    val navController = rememberNavController()

    // Background container
    Box(
        modifier = Modifier.background(Color(0xFFEEEEEE))
    ) {
        Scaffold(containerColor = Color.Transparent) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues)
            ) {

                // Splash screen
                composable("splash") {
                    SplashScreen(
                        onNavigateToGallery = {
                            navController.navigate("gallery") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                //create‑then‑navigate route
                composable("draw/new") {
                    // grab your ViewModel
                    val dvm: DrawingFileViewModel =
                        viewModel(factory = DrawingViewModelProvider.Factory)

                    LaunchedEffect(Unit) {
                        // create the file, then immediately navigate away
                        val drawing = dvm.createFile("Drawing_${System.currentTimeMillis()}")
                        val path    = Uri.encode(drawing.filePath)
                        navController.navigate("draw/$path/${drawing.fileName}") {
                            popUpTo("draw/new") { inclusive = true }
                        }
                    }
                }

                // Draw screen (existing file)
                composable("draw/{filepath}/{filename}") { backStackEntry ->
                    val filePath = backStackEntry.arguments?.getString("filepath").orEmpty()
                    val fileName = backStackEntry.arguments?.getString("filename").orEmpty()

                    DrawScreen(
                        navController = navController,
                        filePath = filePath,
                        fileName = fileName
                    )
                }

                // Gallery screen
                composable("gallery") {
                    GalleryScreen(navController = navController)
                }
            }
        }
    }
}
