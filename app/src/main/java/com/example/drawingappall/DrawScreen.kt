package com.example.drawingappall

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun DrawScreen(viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
               navController: NavController,
               filePath: String, fileName: String) {

    //Attempt to load
    //LaunchedEffect allows edits to not be lost when view reloads
    LaunchedEffect(filePath, fileName) {
        viewModel.loadDrawing(filePath, fileName)
    }


    val bitmap by viewModel.bitmap.collectAsState()
    val shapeSize by viewModel.shapeSize.collectAsState()
    val color by viewModel.color.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFEEEEEE)) // gray background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // push all content down to center the UI on the screen
        // there is another one at the bottom to push up
        Spacer(modifier = Modifier.weight(1f))

        // title + quit ("x") and save button
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fileName,
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            IconButton(onClick = {viewModel.saveDrawing(filePath, fileName)}) {
                Icon(painter = painterResource(id = R.drawable.save_icon) ,contentDescription = "Save", tint = Color.Gray)
            }

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
                        // detect tapping
                        detectTapGestures { tapOffset ->
                            viewModel.drawOnCanvas(
                                x = tapOffset.x,
                                y = tapOffset.y,
                                viewWidth = size.width,
                                viewHeight = size.height
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        // detect dragging
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
            Text("${shapeSize.toInt()}",
                color = Color.Black, fontSize = 18.sp,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(16.dp))

            // the actual slider with dynamic coloring
            Slider(
                value = shapeSize,
                onValueChange = { viewModel.updateSize(it) },
                valueRange = 5f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("BrushSizeSlider"),
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

        Spacer(modifier = Modifier.height(6.dp))

        DrawShapeUI(viewModel)

        // pushes content upwards to center the UI on the screen
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun DrawShapeUI(viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory)) {
    // header text
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Shapes",
            color = Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // buttons
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val circleIcon = painterResource(id = R.drawable.circle_icon)
        val triangleIcon = painterResource(id = R.drawable.triangle_icon)
        val squareIcon = painterResource(id = R.drawable.square_icon)

        // Square Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Square) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {

            Image(painter = squareIcon, contentDescription = "Square Button")
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Circle Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Circle) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {

            Image(painter = circleIcon, contentDescription = "Circle Button")
        }
        Spacer(modifier = Modifier.width(4.dp))

        // Triangle Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Triangle) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {

            Image(painter = triangleIcon, contentDescription = "Triangle Button")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawScreenPreview() {
    val navController = rememberNavController() // Mock NavController for previewing
    val viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory) // Default ViewModel instance

    DrawScreen(
        viewModel = viewModel,
        navController = navController,
        "MyDrawing",
        "default_preview.png"
    )
}