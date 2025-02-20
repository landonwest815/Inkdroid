package com.example.drawingappall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * A custom `View` that displays a given `Bitmap`.
 * Supports dynamic updates by calling `setBitmap()`.
 */
class BitmapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var bitmap: Bitmap? = null

    // Lazily initialized Rect, ensuring it's created only when accessed (after size is set)
    private val rect: Rect by lazy { Rect(0, 0, width, height) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the bitmap to fit the entire view
        bitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
        }
    }

    // Sets a new `Bitmap` and requests a redraw.
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate() // Triggers `onDraw()` to refresh the view
    }
}