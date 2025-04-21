package com.example.drawingappall.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.drawingappall.R
import com.example.drawingappall.viewModels.*

/**
 * Main drawing screen containing canvas, tools, and controls.
 */
@Composable
fun DrawScreen(
    vm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
    navController: NavController,
    filePath: String,
    fileName: String
) {
    var currentFileName by remember { mutableStateOf(fileName) }

    LaunchedEffect(filePath, currentFileName) {
        viewModel.loadDrawing(filePath, currentFileName)
    }

    val bitmap by viewModel.bitmap.collectAsState()
    val shapeSize by viewModel.shapeSize.collectAsState()
    val color by viewModel.color.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFEEEEEE))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DrawScreenHeader(
            currentFileName = currentFileName,
            onRename = { newName, resultCallback ->
                viewModel.saveDrawing(filePath, currentFileName)
                vm.renameDrawing(filePath, currentFileName, newName) { success ->
                    if (success) currentFileName = newName
                    resultCallback(success)
                }
            },
            onSaveAndClose = {
                viewModel.saveDrawing(filePath, currentFileName)
                navController.popBackStack()
            }
        )

        DrawingCanvas(bitmap = bitmap, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        SizeSlider(shapeSize = shapeSize, onSizeChange = viewModel::updateSize, color = color)

        Spacer(modifier = Modifier.height(4.dp))

        BrushShapeSelector(viewModel = viewModel, color = color)

        Spacer(modifier = Modifier.height(16.dp))

        ActionButtons(
            onPickColor = viewModel::pickColor,
            onReset = viewModel::resetCanvas,
            onBlur = viewModel::blur,
            onSharpen = viewModel::sharpen,
            color = color
        )
    }
}

@Composable
private fun DrawScreenHeader(
    currentFileName: String,
    onRename: (String, (Boolean) -> Unit) -> Unit,
    onSaveAndClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onSaveAndClose,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Save & Close",
                tint = Color.Gray
            )
        }

        RenameableFileName(
            fileName = currentFileName,
            onRename = onRename
        )
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
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clickable {
                showDialog = true
                newName = fileName
                showError = false
            }
            .testTag("FileNameDisplay")
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
                        singleLine = true,
                        modifier = Modifier.testTag("RenameInput")
                    )
                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Name already exists", color = Color.Red, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Text("Rename", modifier = Modifier.clickable {
                    if (newName.isNotBlank() && newName != fileName) {
                        onRename(newName) { success ->
                            if (success) showDialog = false else showError = true
                        }
                    } else {
                        showDialog = false
                    }
                }.testTag("RenameConfirm"))
            },
            dismissButton = {
                Text("Cancel", modifier = Modifier.clickable { showDialog = false })
            }
        )
    }
}

@Composable
private fun DrawingCanvas(bitmap: Bitmap, viewModel: DrawingViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context -> BitmapView(context, null).apply { setBitmap(bitmap) } },
            update = { view -> view.setBitmap(bitmap) },
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        viewModel.drawOnCanvas(
                            tapOffset.x, tapOffset.y,
                            viewWidth = size.width, viewHeight = size.height
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        viewModel.drawOnCanvas(
                            change.position.x, change.position.y,
                            viewWidth = size.width, viewHeight = size.height
                        )
                    }
                }
        )
    }
}

@Composable
private fun SizeSlider(shapeSize: Float, onSizeChange: (Float) -> Unit, color: Color) {
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Size", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        Text("${shapeSize.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))

        Slider(
            value = shapeSize,
            onValueChange = onSizeChange,
            valueRange = 5f..100f,
            modifier = Modifier.fillMaxWidth().testTag("BrushSizeSlider"),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFFBDBDBD)
            )
        )
    }
}

@Composable
fun BrushShapeSelector(
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
    color: Color
) {
    val selected by viewModel.brushShape.collectAsState()
    val icons = listOf(
        BrushShape.Square to painterResource(id = R.drawable.square),
        BrushShape.Circle to painterResource(id = R.drawable.circle),
        BrushShape.Triangle to painterResource(id = R.drawable.triangle)
    )

    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Shape", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))

        icons.forEach { (shape, icon) ->
            ShapeButton(shape, selected == shape, icon, color) {
                viewModel.changeShape(shape)
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun RowScope.ShapeButton(
    shape: BrushShape,
    selected: Boolean,
    icon: Painter,
    tintColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier.weight(1f).testTag("${shape.name}Button")
    ) {
        Image(
            painter = icon,
            contentDescription = "$shape Button",
            colorFilter = ColorFilter.tint(if (selected) tintColor else Color(0xFFBDBDBD))
        )
    }
}

@Composable
private fun ActionButtons(
    onPickColor: () -> Unit,
    onReset: () -> Unit,
    onBlur: () -> Unit,
    onSharpen: () -> Unit,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPickColor,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Change Color", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reset Canvas", color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBlur,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Blur", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onSharpen,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Sharpen", color = Color.White)
            }
        }
    }
}
