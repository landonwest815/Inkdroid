package com.example.drawingappall

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class BitmapModel :ViewModel() {

    val bitmap = MutableStateFlow<Bitmap>(Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888))
    private val bitmapCanvas = MutableStateFlow<Canvas>(Canvas(bitmap.value))

    init
    {
        val paint = Paint()
        paint.color = Color.WHITE

        // initialize bitmap white
        val bitmapValue = bitmap.value
        bitmapCanvas.value.drawRect(0f,0f, bitmapValue.width.toFloat(), bitmapValue.height.toFloat(), paint)
    }

    public fun getBitmap() : Bitmap
    {
        return bitmap.value
    }

    fun drawCircle(color :Color, scale : Float, paint : Paint)
    {
        paint.color = color.toArgb()
        val bitmapValue = bitmap.value

        bitmapCanvas.value.drawCircle(scale*bitmapValue.width, scale*bitmapValue.height,
            0.5f*scale*bitmapValue.width, paint)
    }

    fun drawPen(x : Float, y : Float, paint : Paint)
    {
        val testPaint = Paint()
        testPaint.color = Color.BLACK
        bitmapCanvas.value.drawPoint(x, y, testPaint)

        val bitmapValue = bitmap.value
        Log.d("DrawPixel", "Pixel at ($x, $y)")
    }
}