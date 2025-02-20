package com.example.drawingappall

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun DrawScreen(viewModel: DrawingViewModel = viewModel(), navController: NavController) {

    // get our data from the viewModel
    val bitmap by viewModel.bitmap.collectAsState()
    val circleSize by viewModel.circleSize.collectAsState()
    val color by viewModel.color.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)) // gray background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // push all content down to center the UI on the screen
        // there is another one at the bottom to push up
        Spacer(modifier = Modifier.weight(1f))

        // title + quit button "x"
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Drawing",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.weight(1f))

            // "x" button
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }

        // drawable canvas
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // fill most of the screen width
                .aspectRatio(1f),       // enforce square size
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context -> BitmapView(context, null).apply { setBitmap(bitmap) } },
                update = { view -> view.setBitmap(bitmap) },
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .background(Color.White)
                    // this is what recognizes the touch events from the user
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

        // size slider
        Row(modifier = Modifier.padding(horizontal = 32.dp), verticalAlignment = Alignment.CenterVertically) {

            // ex: size: 50
            Text("Size",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("${circleSize.toInt()}",
                color = Color.Black, fontSize = 18.sp,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(16.dp))

            // the actual slider with dynamic coloring
            Slider(
                value = circleSize,
                onValueChange = { viewModel.updateSize(it) },
                valueRange = 5f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = color,  // the circle
                    activeTrackColor = color,  // left side of circle
                    inactiveTrackColor = Color(0xFFBDBDBD)  // right side of circle
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // color button + reset button
        Row(modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically) {

            // color button
            Button(
                onClick = { viewModel.pickColor() },
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Change Color", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // reset button
            Button(
                onClick = { viewModel.resetCanvas() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reset Canvas", color = Color.White)
            }
        }

        // pushes content upwards to center the UI on the screen
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun DrawScreenPreview() {
    val navController = rememberNavController() // Mock NavController for previewing
    val viewModel: DrawingViewModel = viewModel() // Default ViewModel instance

    DrawScreen(
        viewModel = viewModel,
        navController = navController
    )
}