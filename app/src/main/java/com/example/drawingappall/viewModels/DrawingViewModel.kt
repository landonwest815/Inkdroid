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
 * ViewModel for managing a drawing canvas bitmap.
 * Supports brush settings, drawing operations, and native image filters.
 */
class DrawingViewModel(
    private val repository: DrawingsRepository
) : ViewModel() {

    companion object {
        private const val BITMAP_WIDTH = 800
        private const val BITMAP_HEIGHT = 800

        /**
         * Creates a blank ARGB_8888 bitmap of fixed size.
         */
        private fun createEmptyBitmap(): Bitmap =
            Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
    }

    // -------- State Flows --------

    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    private val _color = MutableStateFlow(Color.Black)
    val color: StateFlow<Color> = _color

    private val _shapeSize = MutableStateFlow(66f)
    val shapeSize: StateFlow<Float> = _shapeSize

    private val _brushShape = MutableStateFlow(BrushShape.Circle)
    val brushShape: StateFlow<BrushShape> = _brushShape

    // -------- Drawing Utilities --------

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bitmapEditor = ImageModification()

    // -------- Canvas Lifecycle --------

    /**
     * Load bitmap from disk or reset to blank if missing.
     */
    fun loadDrawing(filePath: String, fileName: String) {
        val loaded = repository.loadFromDisk(filePath, fileName)
        _bitmap.value = loaded ?: createEmptyBitmap()
    }

    /**
     * Save current canvas bitmap to disk.
     */
    fun saveDrawing(filePath: String, fileName: String) {
        repository.saveToDisk(filePath, fileName, _bitmap.value)
    }

    /**
     * Reset canvas to a blank bitmap.
     */
    fun resetCanvas() {
        _bitmap.value = createEmptyBitmap()
    }

    // -------- Brush Configuration --------

    /**
     * Update brush size, constrained between 5 and 100.
     */
    fun updateSize(newSize: Float) {
        _shapeSize.update { newSize.coerceIn(5f, 100f) }
    }

    /**
     * Change brush shape and trigger a preview refresh.
     */
    fun changeShape(brushShape: BrushShape) {
        _brushShape.value = brushShape
        // Force UI recomposition for shape preview
        drawOnCanvas(-1f, -1f, 1, 1)
    }

    /**
     * Select a random RGB color for the brush.
     */
    fun pickColor() {
        _color.update {
            Color(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat(),
                1f
            )
        }
    }

    // -------- Native Image Filters --------

    fun blur() {
        Log.d("DrawingViewModel", "Applying blur filter")
        bitmapEditor.blur(_bitmap.value)
        val cfg = _bitmap.value.config ?: Bitmap.Config.ARGB_8888
        _bitmap.value = _bitmap.value.copy(cfg, true)
    }

    fun sharpen() {
        Log.d("DrawingViewModel", "Applying sharpen filter")
        bitmapEditor.sharpen(_bitmap.value)
        val cfg = _bitmap.value.config ?: Bitmap.Config.ARGB_8888
        _bitmap.value = _bitmap.value.copy(cfg, true)
    }


    // -------- Drawing on Canvas --------

    /**
     * Draw current brush shape onto the bitmap at given view coords.
     * @param x touch X in view coords
     * @param y touch Y in view coords
     * @param viewWidth width of the drawing view
     * @param viewHeight height of the drawing view
     */
    fun drawOnCanvas(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val srcBitmap = _bitmap.value
        val mutable = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val scaledX = x * mutable.width / viewWidth
        val scaledY = y * mutable.height / viewHeight

        paint.color = _color.value.toAndroidColor()
        drawShape(scaledX, scaledY, paint, canvas)

        _bitmap.value = mutable
    }

    /**
     * Draws the selected brush shape at bitmap coords.
     */
    private fun drawShape(scaledX: Float, scaledY: Float, paint: Paint, canvas: Canvas) {
        val size = _shapeSize.value
        when (_brushShape.value) {
            BrushShape.Circle -> canvas.drawCircle(scaledX, scaledY, size, paint)
            BrushShape.Square -> canvas.drawRect(
                scaledX - size, scaledY - size,
                scaledX + size, scaledY + size,
                paint
            )
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

    // -------- Helpers --------

    /**
     * Convert Compose Color to Android ARGB int.
     */
    private fun Color.toAndroidColor(): Int = AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )

    // -------- ViewModel Factory --------

    /**
     * Factory for injecting repository dependency.
     */
    object DrawingViewModelProvider {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
                DrawingViewModel(app.repository)
            }
        }
    }
}
