package com.example.drawingappall.jni

import android.graphics.Bitmap

/**
 * JNI bridge for native image processing operations.
 * Declares native methods for in-place Bitmap manipulation.
 */
class ImageModification {

    /**
     * Applies a Gaussian blur to the provided bitmap.
     * Operates in-place on the Bitmap's pixel data.
     * @param bitmap Android Bitmap to blur
     */
    external fun blur(bitmap: Bitmap)

    /**
     * Applies a sharpening filter to the provided bitmap.
     * Operates in-place on the Bitmap's pixel data.
     * @param bitmap Android Bitmap to sharpen
     */
    external fun sharpen(bitmap: Bitmap)

    companion object {
        init {
            // Load native library containing implementations of blur and sharpen
            System.loadLibrary("imagemodification")
        }
    }
}
