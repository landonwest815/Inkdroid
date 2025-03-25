package com.example.drawingappall

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Enum for different brush shapes
 */
enum class BrushShape
{
    Square,
    Circle,
    Triangle
}

/**
 * ViewModel for managing a drawable canvas in a Jetpack Compose app.
 * Handles drawing, color selection, and bitmap management.
 */
class DrawingViewModel(private val repository: DrawingsRepository) : ViewModel() {

    //only do when not loading file
    //Holds the bitmap that serves as the drawing surface
    private val _bitmap = MutableStateFlow(createEmptyBitmap())
    val bitmap: StateFlow<Bitmap> = _bitmap

    //Current drawing color (default: Black)
    private val _color = MutableStateFlow(Color.Black)
    val color: StateFlow<Color> = _color

    //Shape size for drawing (default: 50px, constrained between 5px and 100px)
    private val _shapeSize = MutableStateFlow(50f)
    val shapeSize: StateFlow<Float> = _shapeSize

    //Brush shape for drawing
    private val _brushShape = MutableStateFlow(BrushShape.Circle)
    val brushShape: StateFlow<BrushShape> = _brushShape

    //Paint object for rendering shapes
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

    //Saves the bitmap using the repository
    fun saveDrawing(filePath : String, fileName : String){
        //save/update bitmap file
        repository.saveDrawing(filePath, fileName, bitmap.value)
    }

    //Loads a image file into the bitmap
    fun loadDrawing(filePath : String, fileName : String){
        //load file as bitmap
        //always load file created by file view model
        val loadedBitmap = repository.loadDrawing(filePath, fileName)

        //Load bitmap
        if (loadedBitmap != null)
        {
            _bitmap.value = loadedBitmap
        }
        //New file, empty bitmap
        else
        {
            _bitmap.value = createEmptyBitmap()
        }
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

    //Changes shape to an inputted shape
    public fun changeShape(brushShape : BrushShape) {
        _brushShape.value = brushShape
        drawOnCanvas(-99999f, -99999f, 100, 100)
    }

     //Updates the size of the drawing circle with a passed in float value
    fun updateSize(newSize: Float) {
        _shapeSize.update { newSize.coerceIn(5f, 100f) }
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

        // Set the paint color and draw the shape
        paint.color = _color.value.toAndroidColor()
        drawShape(scaledX, scaledY, paint, canvas)

        // Assign the updated bitmap to trigger recomposition
        _bitmap.value = newBitmap
    }

    /**
     * Draws a shape given the current brushShape.
     */
    private fun drawShape(scaledX : Float, scaledY : Float, paint: Paint, canvas : Canvas)
    {
        val shape = _brushShape.value
        val size = _shapeSize.value

        when (shape) {
            BrushShape.Circle -> {
                canvas.drawCircle(scaledX, scaledY, _shapeSize.value, paint)
            }
            BrushShape.Square -> {
                canvas.drawRect(
                    scaledX - size, scaledY - size,
                    scaledX + size, scaledY + size,
                    paint
                )
            }
            BrushShape.Triangle -> {
                // Create triangle path and draw it
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

    object DrawingViewModelProvider {
        val Factory = viewModelFactory {
            initializer {
                DrawingViewModel(
                    //fetches the application singleton
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                            //and then extracts the repository in it
                            as AllApplication).DrawingsRepository
                )
            }
        }
    }
}
