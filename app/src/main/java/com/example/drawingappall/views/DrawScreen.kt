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


/**
 * Host composable for the drawing screen: loads the bitmap, shows header, canvas, and controls.
 */
@Composable
fun DrawScreen(
    vm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
    navController: NavController,
    filePath: String,
    fileName: String
) {
    var currentName by remember { mutableStateOf(fileName) }

    // Load the bitmap whenever path or name changes
    LaunchedEffect(filePath, currentName) {
        viewModel.loadDrawing(filePath, currentName)
    }

    val bitmap by viewModel.bitmap.collectAsState()
    val shapeSize by viewModel.shapeSize.collectAsState()
    val brushColor by viewModel.color.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val horizontalPadding = if (screenWidth >= 720) 24.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DrawScreenHeader(
                currentFileName = currentName,
                onRename = { newName, callback ->
                    viewModel.saveDrawing(filePath, currentName)
                    vm.renameFile(filePath, currentName, newName) { success ->
                        if (success) currentName = newName
                        callback(success)
                    }
                },
                onSaveAndClose = {
                    viewModel.saveDrawing(filePath, currentName)
                    navController.popBackStack()
                }
            )

            DrawingCanvas(bitmap = bitmap, viewModel = viewModel)

            DrawControls(
                shapeSize = shapeSize,
                onSizeChange = viewModel::updateSize,
                brushColor = brushColor,
                viewModel = viewModel,
                onPickColor = viewModel::pickColor,
                onReset = viewModel::resetCanvas,
                onBlur = viewModel::blur,
                onSharpen = viewModel::sharpen
            )
        }
    }
}


/*----- Header -----*/

/** Displays back button and renameable file name. */
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

/** Clickable file name with rename dialog. */
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
                Text(
                    "Rename",
                    modifier = Modifier
                        .clickable {
                            if (newName.isNotBlank() && newName != fileName) {
                                onRename(newName) { success ->
                                    if (success) showDialog = false else showError = true
                                }
                            } else {
                                showDialog = false
                            }
                        }
                        .testTag("RenameConfirm")
                )
            },
            dismissButton = {
                Text("Cancel", modifier = Modifier.clickable { showDialog = false })
            }
        )
    }
}


/*----- Canvas -----*/

/** Shows the drawing canvas and handles touch input. */
@Composable
private fun DrawingCanvas(
    bitmap: Bitmap,
    viewModel: DrawingViewModel
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val sizeDp = if (this.maxWidth > 500.dp) 500.dp else maxWidth

        Box(
            modifier = Modifier
                .width(sizeDp)
                .aspectRatio(1f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx -> BitmapView(ctx, null).apply { setBitmap(bitmap) } },
                update = { it.setBitmap(bitmap) },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            viewModel.drawOnCanvas(pos.x, pos.y, viewWidth = size.width, viewHeight = size.height)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            viewModel.drawOnCanvas(change.position.x, change.position.y, viewWidth = size.width, viewHeight = size.height)
                        }
                    }
            )
        }
    }
}


/*----- Controls -----*/

/**
 * Groups size slider, shape selector, and action buttons.
 * Layout adjusts for tablet vs phone widths.
 */
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
    val isTablet = LocalConfiguration.current.screenWidthDp >= 720

    if (isTablet) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    SizeSlider(shapeSize, onSizeChange, brushColor)
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    BrushShapeSelector(viewModel = viewModel, color = brushColor)
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
            SizeSlider(shapeSize, onSizeChange, brushColor)
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


/*----- Reusable Components -----*/

/** Displays and styles a simple button with text. */
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

/** Slider for brush size with label and value display. */
@Composable
private fun SizeSlider(
    shapeSize: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Size", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text("${shapeSize.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(16.dp))
        Slider(
            value = shapeSize,
            onValueChange = onSizeChange,
            valueRange = 5f..100f,
            modifier = Modifier
                .weight(1f)
                .testTag("BrushSizeSlider"),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFFBDBDBD)
            )
        )
    }
}

/** Row of shape-selection buttons for different brush shapes. */
@Composable
fun BrushShapeSelector(
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.DrawingViewModelProvider.Factory),
    color: Color,
    modifier: Modifier = Modifier
) {
    val selectedShape by viewModel.brushShape.collectAsState()
    val icons = listOf(
        BrushShape.Square to painterResource(R.drawable.square),
        BrushShape.Circle to painterResource(R.drawable.circle),
        BrushShape.Triangle to painterResource(R.drawable.triangle)
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Shape", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        icons.forEach { (shape, icon) ->
            ShapeButton(
                shape = shape,
                selected = (shape == selectedShape),
                icon = icon,
                tintColor = color
            ) {
                viewModel.changeShape(shape)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

/** Individual shape button with icon and selection tint. */
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
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .testTag("${shape.name}Button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Image(
            painter = icon,
            contentDescription = "$shape Button",
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(if (selected) tintColor else Color(0xFFBDBDBD))
        )
    }
}
