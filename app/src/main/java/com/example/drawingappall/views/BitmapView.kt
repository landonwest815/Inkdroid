package com.example.drawingappall.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * A custom View for rendering a provided [Bitmap] to fill the entire view bounds.
 * Use [setBitmap] to update the image dynamically.
 */
class BitmapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var bitmap: Bitmap? = null

    // Defines the drawing bounds (lazy to ensure width/height are available)
    private val rect: Rect by lazy {
        Rect(0, 0, width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
        }
    }

    /**
     * Sets a new bitmap to be displayed and triggers a redraw.
     */
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate()
    }
}
