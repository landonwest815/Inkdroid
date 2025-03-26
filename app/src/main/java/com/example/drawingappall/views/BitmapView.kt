package com.example.drawingappall.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * A custom View that displays a given Bitmap.
 * You can update the image dynamically using setBitmap().
 */
class BitmapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Paint used for rendering the bitmap
    private val paint = Paint()

    // The bitmap to display
    private var bitmap: Bitmap? = null

    // Rect defining the area to draw the bitmap (entire view bounds)
    private val rect: Rect by lazy {
        Rect(0, 0, width, height)
    }

    // Called automatically when the view needs to be drawn
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // If a bitmap is set, draw it to fill the entire view
        bitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
        }
    }

    // Public method to update the displayed bitmap
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate() // Request a redraw so onDraw() is called again
    }
}