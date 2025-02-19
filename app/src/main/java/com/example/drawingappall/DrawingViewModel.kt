package com.example.drawingappall

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * ViewModel for managing a drawable canvas in a Jetpack Compose app.
 * Handles drawing, color selection, and bitmap management.
 */
class DrawingViewModel : ViewModel() {

    // Holds the bitmap that serves as the drawing surface
    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    // Current drawing color (default: Black)
    private val _color = MutableStateFlow(Color.Black)
    val color: StateFlow<Color> = _color

    // Circle size for drawing (default: 50px, constrained between 5px and 100px)
    private val _circleSize = MutableStateFlow(50f)
    val circleSize: StateFlow<Float> = _circleSize

    // Paint object for rendering shapes
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    companion object {
        private const val BITMAP_WIDTH = 800
        private const val BITMAP_HEIGHT = 800

        //Creates an empty bitmap with a transparent background.
        private fun createEmptyBitmap(): Bitmap =
            Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
    }

    //Picks a random color and updates the current drawing color
    fun pickColor() {
        _color.update {
            it.copy(
                red = Random.nextFloat(),
                green = Random.nextFloat(),
                blue = Random.nextFloat()
            )
        }
    }


     //Updates the size of the drawing circle with a passed in float value
    fun updateSize(newSize: Float) {
        _circleSize.update { newSize.coerceIn(5f, 100f) }
    }


    /**
     * Draws a circle at the specified screen coordinates.
     * @param x Touch X-coordinate (relative to the view).
     * @param y Touch Y-coordinate (relative to the view).
     * @param viewWidth The width of the view displaying the bitmap.
     * @param viewHeight The height of the view displaying the bitmap.
     */
    fun drawOnCanvas(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val oldBitmap = _bitmap.value

        // Create a new bitmap based on the existing one
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)

        // Scale touch coordinates to match the bitmap size
        val scaledX = x * newBitmap.width / viewWidth
        val scaledY = y * newBitmap.height / viewHeight

        // Set the paint color and draw the circle
        paint.color = _color.value.toAndroidColor()
        canvas.drawCircle(scaledX, scaledY, _circleSize.value, paint)

        // Assign the updated bitmap to trigger recomposition
        _bitmap.value = newBitmap
    }

    // Resets the canvas by replacing the current bitmap with a new empty one.
    fun resetCanvas() {
        _bitmap.value = createEmptyBitmap()
    }

    // Converts a Jetpack Compose [Color] to an Android color integer.
    private fun Color.toAndroidColor(): Int = AndroidColor.argb(
        255,
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}