package com.example.drawingappall.viewModels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.DrawingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Enum for different brush shapes
 */
enum class BrushShape {
    Square,
    Circle,
    Triangle
}

/**
 * ViewModel for managing a drawable canvas in a Jetpack Compose app.
 * Handles drawing logic, color selection, brush shapes, and bitmap persistence.
 */
class DrawingViewModel(
    private val repository: DrawingsRepository
) : ViewModel() {

    // Holds the bitmap that acts as the canvas
    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    // Current drawing color (default: Black)
    private val _color = MutableStateFlow(Color.Black)
    val color: StateFlow<Color> = _color

    // Size of brush strokes (default: 66f, limited between 5 and 100)
    private val _shapeSize = MutableStateFlow(66f)
    val shapeSize: StateFlow<Float> = _shapeSize

    // Current selected brush shape (default: Circle)
    private val _brushShape = MutableStateFlow(BrushShape.Circle)
    val brushShape: StateFlow<BrushShape> = _brushShape

    // Paint object for rendering
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    companion object {
        private const val BITMAP_WIDTH = 800
        private const val BITMAP_HEIGHT = 800

        // Creates a new transparent bitmap
        private fun createEmptyBitmap(): Bitmap =
            Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
    }

    // Loads an existing bitmap from storage or creates a new empty one
    fun loadDrawing(filePath: String, fileName: String) {
        val loadedBitmap = repository.loadDrawing(filePath, fileName)
        _bitmap.value = loadedBitmap ?: createEmptyBitmap()
    }

    // Saves the current bitmap to file
    fun saveDrawing(filePath: String, fileName: String) {
        repository.saveDrawing(filePath, fileName, bitmap.value)
    }

    // Resets canvas to a blank state
    fun resetCanvas() {
        _bitmap.value = createEmptyBitmap()
    }

    // Updates brush size while keeping it within allowed limits
    fun updateSize(newSize: Float) {
        _shapeSize.update { newSize.coerceIn(5f, 100f) }
    }

    // Updates the selected brush shape and triggers a fake draw to show UI change
    fun changeShape(brushShape: BrushShape) {
        _brushShape.value = brushShape
        drawOnCanvas(-99999f, -99999f, 100, 100) // force UI to reflect shape change
    }

    // Randomly changes the current drawing color
    fun pickColor() {
        _color.update {
            it.copy(
                red = Random.nextFloat(),
                green = Random.nextFloat(),
                blue = Random.nextFloat()
            )
        }
    }


    /**
     * Draws on the canvas at given coordinates, scaled to bitmap dimensions.
     *
     * @param x Touch x-coordinate (in view space)
     * @param y Touch y-coordinate (in view space)
     * @param viewWidth Width of the view
     * @param viewHeight Height of the view
     */
    fun drawOnCanvas(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val oldBitmap = _bitmap.value
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)

        // Scale coordinates to bitmap space
        val scaledX = x * newBitmap.width / viewWidth
        val scaledY = y * newBitmap.height / viewHeight

        // Draw the selected shape
        paint.color = _color.value.toAndroidColor()
        drawShape(scaledX, scaledY, paint, canvas)

        _bitmap.value = newBitmap
    }

    /**
     * Draws the shape on the canvas based on the selected brush shape.
     */
    private fun drawShape(scaledX: Float, scaledY: Float, paint: Paint, canvas: Canvas) {
        val size = _shapeSize.value

        when (_brushShape.value) {
            BrushShape.Circle -> {
                canvas.drawCircle(scaledX, scaledY, size, paint)
            }

            BrushShape.Square -> {
                canvas.drawRect(
                    scaledX - size, scaledY - size,
                    scaledX + size, scaledY + size,
                    paint
                )
            }

            BrushShape.Triangle -> {
                val path = Path().apply {
                    moveTo(scaledX, scaledY - size)
                    lineTo(scaledX - size, scaledY + size)
                    lineTo(scaledX + size, scaledY + size)
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    // Converts Jetpack Compose Color to Android ARGB Int
    private fun Color.toAndroidColor(): Int = AndroidColor.argb(
        255,
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )

    /**
     * ViewModel Factory for DrawingViewModel.
     * Pulls the repository from the custom Application class.
     */
    object DrawingViewModelProvider {
        val Factory = viewModelFactory {
            initializer {
                DrawingViewModel(
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AllApplication)
                        .drawingsRepository
                )
            }
        }
    }
}
