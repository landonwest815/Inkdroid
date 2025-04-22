package com.example.drawingappall.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * Custom View that scales and draws a Bitmap to fill its bounds.
 */
class BitmapView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Paint object for bitmap rendering
    private val paint = Paint()

    // Currently displayed bitmap
    private var bitmap: Bitmap? = null

    // Destination rectangle matching view dimensions
    private val destRect: Rect by lazy { Rect(0, 0, width, height) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            // Draw bitmap scaled to fill view
            canvas.drawBitmap(it, null, destRect, paint)
        }
    }

    /**
     * Set a new bitmap and request redraw.
     */
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate()
    }
}
