package com.example.drawingappall

import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ColorTests {

    @Test
    fun defaultColor(){
        val vm = DrawingViewModel()
        val black = Color.Black
        val vmColor = vm.color.value

        assertEquals(black, vmColor)
    }
}