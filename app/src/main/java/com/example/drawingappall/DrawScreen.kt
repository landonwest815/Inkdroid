package com.example.drawingappall

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun DrawScreen(
    vm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
    navController: NavController,
    filePath: String, fileName: String) {

    // allows for real time UI updates when file name is changed
    var currentFileName by remember { mutableStateOf(fileName) }

    // allows for edits to not be lost when UI is refreshed during color change
    LaunchedEffect(filePath, currentFileName) {
        viewModel.loadDrawing(filePath, currentFileName)
    }

    // collect the viewmodel data
    val bitmap by viewModel.bitmap.collectAsState()
    val shapeSize by viewModel.shapeSize.collectAsState()
    val color by viewModel.color.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFEEEEEE)) // gray background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // back button (save, as well)
            IconButton(
                onClick = {
                    // save AND close
                    viewModel.saveDrawing(filePath, currentFileName)
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Save & Close",
                    tint = Color.Gray
                )
            }

            // drawing title (clickable)
            RenameableFileName(
                fileName = currentFileName,
                onRename = { newName, resultCallback ->
                    vm.renameDrawing(filePath, currentFileName, newName) { success ->
                        if (success) {
                            currentFileName = newName
                        }
                        resultCallback(success)
                    }
                }
            )
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

        DrawShapeUI(viewModel, color)

        Spacer(modifier = Modifier.height(16.dp))

        // color button + reset button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // color button
            Button(
                onClick = { viewModel.pickColor() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Change Color", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // reset button
            Button(
                onClick = { viewModel.resetCanvas() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reset Canvas", color = Color.White)
            }
        }
    }
}

@Composable
fun RenameableFileName(
    fileName: String,
    onRename: (String, (Boolean) -> Unit) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(fileName) }
    var showError by remember { mutableStateOf(false) }

    Text(
        text = fileName,
        color = Color.Gray,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clickable {
                showDialog = true
                newName = fileName
                showError = false
            }
            .padding(top = 8.dp)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Rename Drawing") },
            text = {
                Column {
                    TextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            showError = false
                        },
                        singleLine = true
                    )
                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Name already exists",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    "Rename",
                    modifier = Modifier.clickable {
                        if (newName.isNotBlank() && newName != fileName) {
                            onRename(newName) { success ->
                                if (success) {
                                    showDialog = false
                                } else {
                                    showError = true
                                }
                            }
                        } else {
                            showDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                Text(
                    "Cancel",
                    modifier = Modifier.clickable {
                        showDialog = false
                    }
                )
            }
        )
    }
}


@Composable
fun DrawShapeUI(viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory), color: Color) {

    // buttons
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text("Shape",
            color = Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold)

        val selected by viewModel.brushShape.collectAsState()
        val circleIcon = painterResource(id = R.drawable.circle)
        val triangleIcon = painterResource(id = R.drawable.triangle)
        val squareIcon = painterResource(id = R.drawable.square)

        Spacer(modifier = Modifier.width(8.dp))

        // Square Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Square) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = squareIcon,
                contentDescription = "Square Button",
                colorFilter = ColorFilter.tint(
                    if (selected == BrushShape.Square) color else Color(0xFFBDBDBD)
                )
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Circle Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Circle) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = circleIcon,
                contentDescription = "Circle Button",
                colorFilter = ColorFilter.tint(
                    if (selected == BrushShape.Circle) color else Color(0xFFBDBDBD)
                )
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Triangle Button
        Button(
            onClick = { viewModel.changeShape(BrushShape.Triangle) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = triangleIcon,
                contentDescription = "Triangle Button",
                colorFilter = ColorFilter.tint(
                    if (selected == BrushShape.Triangle) color else Color(0xFFBDBDBD)
                )
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DrawScreenPreview() {
//    val navController = rememberNavController() // Mock NavController for previewing
//    val viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory) // Default ViewModel instance
//
//    DrawScreen(
//        viewModel = viewModel,
//        navController = navController,
//        "MyDrawing",
//        "default_preview.png"
//    )
//}