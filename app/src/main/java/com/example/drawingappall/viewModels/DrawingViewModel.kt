package com.example.drawingappall.viewModels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.DrawingsRepository
import com.example.drawingappall.jni.ImageModification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Enum representing available brush shapes.
 */
enum class BrushShape {
    Square,
    Circle,
    Triangle
}

/**
 * ViewModel for managing a bitmap drawing canvas.
 * Supports brush customization, drawing logic, and basic image processing.
 */
class DrawingViewModel(
    private val repository: DrawingsRepository
) : ViewModel() {

    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    private val _color = MutableStateFlow(Color.Black)
    val color: StateFlow<Color> = _color

    private val _shapeSize = MutableStateFlow(66f)
    val shapeSize: StateFlow<Float> = _shapeSize

    private val _brushShape = MutableStateFlow(BrushShape.Circle)
    val brushShape: StateFlow<BrushShape> = _brushShape

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val _bitmapEditor = ImageModification()

    companion object {
        private const val BITMAP_WIDTH = 800
        private const val BITMAP_HEIGHT = 800

        private fun createEmptyBitmap(): Bitmap =
            Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
    }

    /**
     * Loads an existing drawing from disk or initializes an empty bitmap.
     */
    fun loadDrawing(filePath: String, fileName: String) {
        val loadedBitmap = repository.loadDrawing(filePath, fileName)
        _bitmap.value = loadedBitmap ?: createEmptyBitmap()
    }

    /**
     * Saves the current bitmap to disk.
     */
    fun saveDrawing(filePath: String, fileName: String) {
        repository.saveDrawing(filePath, fileName, bitmap.value)
    }

    /**
     * Clears the canvas by replacing it with a new blank bitmap.
     */
    fun resetCanvas() {
        _bitmap.value = createEmptyBitmap()
    }

    /**
     * Updates the current brush size, clamped between 5 and 100.
     */
    fun updateSize(newSize: Float) {
        _shapeSize.update { newSize.coerceIn(5f, 100f) }
    }

    /**
     * Changes the current brush shape and triggers a UI refresh.
     */
    fun changeShape(brushShape: BrushShape) {
        _brushShape.value = brushShape
        drawOnCanvas(-99999f, -99999f, 100, 100) // triggers recomposition for preview
    }

    /**
     * Randomly selects a new drawing color.
     */
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
     * Applies a blur effect using native image processing.
     */
    fun blur() {
        Log.d("DrawingViewModel", "Applying blur")
        _bitmapEditor.blur(bitmap.value)
    }

    /**
     * Applies a sharpen effect using native image processing.
     */
    fun sharpen() {
        Log.d("DrawingViewModel", "Applying sharpen")
        _bitmapEditor.sharpen(bitmap.value)
    }

    /**
     * Draws the current brush shape onto the canvas at the given view coordinates.
     */
    fun drawOnCanvas(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val oldBitmap = _bitmap.value
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)

        val scaledX = x * newBitmap.width / viewWidth
        val scaledY = y * newBitmap.height / viewHeight

        paint.color = _color.value.toAndroidColor()
        drawShape(scaledX, scaledY, paint, canvas)

        _bitmap.value = newBitmap
    }

    /**
     * Draws the selected shape on the canvas at the given coordinates.
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

    /**
     * Converts a Jetpack Compose [Color] to an Android ARGB color integer.
     */
    private fun Color.toAndroidColor(): Int = AndroidColor.argb(
        255,
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )

    /**
     * ViewModel provider for creating [DrawingViewModel] with access to the repository.
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
