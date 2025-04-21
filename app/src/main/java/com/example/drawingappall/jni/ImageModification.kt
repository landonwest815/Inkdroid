package com.example.drawingappall.jni

import android.graphics.Bitmap

/**
 * JNI bridge for native image processing operations.
 * Provides blur and sharpen functions via a native library.
 */
class ImageModification {
    external fun blur(bitmap: Bitmap)
    external fun sharpen(bitmap: Bitmap)

    companion object {
        init {
            System.loadLibrary("imagemodification")
        }
    }
}
