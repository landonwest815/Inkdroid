package com.example.drawingappall

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class DrawingViewModel : ViewModel() {

    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    private val _color = MutableStateFlow(Color(0f, 0f, 0f)) // Default Yellow
    val color: StateFlow<Color> = _color

    private val _circleSize = MutableStateFlow(20f)
    val circleSize: StateFlow<Float> = _circleSize

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    companion object {
        private const val BITMAP_WIDTH = 800
        private const val BITMAP_HEIGHT = 800

        private fun createEmptyBitmap(): Bitmap {
            return Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        }
    }

    fun pickColor() {
        with(Random.Default) {
            _color.value = Color(nextFloat(), nextFloat(), nextFloat())
        }
    }

    fun updateSize(newSize: Float) {
        _circleSize.value = newSize.coerceIn(5f, 100f)
    }

    fun drawOnCanvas(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val bitmapWidth = _bitmap.value.width
        val bitmapHeight = _bitmap.value.height

        // Scale touch coordinates to match the bitmap size
        val scaledX = x * bitmapWidth / viewWidth
        val scaledY = y * bitmapHeight / viewHeight

        val newBitmap = _bitmap.value.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)

        val androidColor = AndroidColor.argb(
            255,
            (_color.value.red * 255).toInt(),
            (_color.value.green * 255).toInt(),
            (_color.value.blue * 255).toInt()
        )

        paint.color = androidColor
        canvas.drawCircle(scaledX, scaledY, _circleSize.value, paint)

        _bitmap.value = newBitmap // ✅ Trigger recomposition
    }
}