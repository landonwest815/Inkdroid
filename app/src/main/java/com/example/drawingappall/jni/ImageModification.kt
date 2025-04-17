package com.example.drawingappall.jni

import android.graphics.Bitmap

class ImageModification {
    external fun blur(bitmap: Bitmap)
    external fun sharpen(bitmap: Bitmap)

    companion object {
        init {
            System.loadLibrary("imagemodification")
        }
    }
}