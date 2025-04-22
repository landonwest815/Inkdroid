package com.example.drawingappall.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalConfiguration
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        val horizontalPadding = if (screenWidthDp >= 720) 24.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            DrawControls(
                shapeSize = shapeSize,
                onSizeChange = viewModel::updateSize,
                brushColor = color,
                viewModel = viewModel,
                onPickColor = viewModel::pickColor,
                onReset = viewModel::resetCanvas,
                onBlur = viewModel::blur,
                onSharpen = viewModel::sharpen
            )
        }
    }
}

@Composable
private fun DrawControls(
    shapeSize: Float,
    onSizeChange: (Float) -> Unit,
    brushColor: Color,
    viewModel: DrawingViewModel,
    onPickColor: () -> Unit,
    onReset: () -> Unit,
    onBlur: () -> Unit,
    onSharpen: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 720

    if (isTablet) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Ensures child alignment
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                    SizeSlider(
                        shapeSize = shapeSize,
                        onSizeChange = onSizeChange,
                        color = brushColor
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                    BrushShapeSelector(
                        viewModel = viewModel,
                        color = brushColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("Change Color", brushColor, onPickColor, Modifier.weight(1f))
                ActionButton("Reset", Color.Red, onReset, Modifier.weight(1f))
                ActionButton("Blur", Color.Black, onBlur, Modifier.weight(1f))
                ActionButton("Sharpen", Color.Black, onSharpen, Modifier.weight(1f))
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SizeSlider(shapeSize = shapeSize, onSizeChange = onSizeChange, color = brushColor)
            BrushShapeSelector(viewModel = viewModel, color = brushColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("Change Color", brushColor, onPickColor, Modifier.weight(1f))
                ActionButton("Reset", Color.Red, onReset, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("Blur", Color.Black, onBlur, Modifier.weight(1f))
                ActionButton("Sharpen", Color.Black, onSharpen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(text, color = Color.White)
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
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Cap max width to 600.dp on large screens (like tablets)
        val canvasWidth = if (this.maxWidth > 500.dp) 500.dp else maxWidth

        Box(
            modifier = Modifier
                .width(canvasWidth)
                .aspectRatio(1f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context -> BitmapView(context, null).apply { setBitmap(bitmap) } },
                update = { view -> view.setBitmap(bitmap) },
                modifier = Modifier
                    .fillMaxSize()
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
}


@Composable
private fun SizeSlider(
    shapeSize: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
            modifier = Modifier.weight(1f).testTag("BrushSizeSlider"),
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
    color: Color,
    modifier: Modifier = Modifier
) {
    val selected by viewModel.brushShape.collectAsState()
    val icons = listOf(
        BrushShape.Square to painterResource(id = R.drawable.square),
        BrushShape.Circle to painterResource(id = R.drawable.circle),
        BrushShape.Triangle to painterResource(id = R.drawable.triangle)
    )

    Row(
        modifier = modifier,
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
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .testTag("${shape.name}Button")
    ) {
        Image(
            painter = icon,
            contentDescription = "$shape Button",
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(if (selected) tintColor else Color(0xFFBDBDBD))
        )
    }
}