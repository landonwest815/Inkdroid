package com.example.drawingappall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    Box( // Wrap everything in a Box to apply full-screen background
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)) // ✅ Background covers entire screen
    ) {
        Scaffold(
            contentColor = Color.Transparent, // Prevents Scaffold from overriding background
            containerColor = Color.Transparent // Ensures full transparency in Scaffold
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "click",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("click") {
                    ClickScreen { navController.navigate("draw") }
                }
                composable("draw") {
                    DrawScreen(navController = navController) // Pass navController
                }
            }
        }
    }
}

@Composable
fun ClickScreen(onNavigateToDraw: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to our\ndrawing app!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Landon Evans\nAndy Chadwick\nLandon West",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(onClick = onNavigateToDraw) {
                Text("Let's Draw")
            }
        }
    }
}

@Composable
fun DrawScreen(viewModel: DrawingViewModel = viewModel(), navController: NavController) {
    val bitmap by viewModel.bitmap.collectAsState()
    val strokeColor by viewModel.color.collectAsState()
    val circleSize by viewModel.circleSize.collectAsState()
    val color by viewModel.color.collectAsState() // ✅ Observe the color

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)) // ✅ Non-canvas background is black
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Drawing",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        // ✅ A Box ensures the Canvas takes available space while staying square
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Adjust width to prevent full screen
                .aspectRatio(1f), // Ensures a square shape
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    CustomView(context, null).apply {
                        setBitmap(bitmap)
                    }
                },
                update = { view ->
                    view.setBitmap(bitmap)
                },
                modifier = Modifier
                    .fillMaxSize() // ✅ Canvas takes full space in the Box
                    .aspectRatio(1f) // ✅ Ensures the canvas is square
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            viewModel.drawOnCanvas(
                                x = change.position.x,
                                y = change.position.y,
                                viewWidth = size.width,
                                viewHeight = size.height
                            )
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Size",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing as needed

                Text(
                    "${circleSize.toInt()}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Adjust spacing as needed

            Slider(
                value = circleSize,
                onValueChange = { viewModel.updateSize(it) },
                valueRange = 5f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color(0xFFBDBDBD)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = { viewModel.pickColor() },
                colors = ButtonDefaults.buttonColors(containerColor = color) // ✅ Uses observed state
            ) {
                Text("Change Color", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp)) // Adjust spacing as needed

            // Reset Canvas Button
            Button(
                onClick = { viewModel.resetCanvas() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // ✅ Ensures contrast
            ) {
                Text("Reset Canvas", color = Color.White) // ✅ Ensures readability
            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

//@Preview(showBackground = true)
//@Composable
//fun AppPreview() {
//    ClickScreen(
//        onNavigateToDraw = {}
//    )
//}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    val navController = rememberNavController() // Mock NavController
    val viewModel: DrawingViewModel = viewModel() // Default ViewModel instance

    DrawScreen(
        viewModel = viewModel,
        navController = navController
    )
}