package com.example.drawingappall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

//constructor takes 2 params, and forwards them to the View super constructor
class CustomView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private var bitmap: Bitmap? = null
    //width/height are 0 when the constructor is called
    //use the lazy delegated property to initialize it on first access, once the size is set
    private val rect: Rect by lazy {Rect(0,0,width, height)}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //?. calls the let method if bitmap is not null
        // let just runs the callback with it = bitmap
        //like if(bitmap != null){canvas.drawBitmap(bitmap!!, ...)}
        bitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
            //canvas.drawBitmap(it, null, Rect(0,0, 500, 500), paint)
        }
    }

    fun setBitmap(bitmap: Bitmap){
        this.bitmap = bitmap
    }
}