package com.example.drawingappall

import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingappall.TestUtils.testVM
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ColorTests {
    // checks that the default color is black when starting app fresh
    @Test
    fun defaultColor(){
        val vm = testVM();

        val black = Color.Black
        val vmColor = vm.color.value

        assertEquals(black, vmColor)
    }

    // checks that picking a random color sets it to a new value
    @Test
    fun pickColorTest() {
        val vm = testVM();

        val initialColor = vm.color.value

        vm.pickColor()

        assertNotEquals(initialColor, vm.color.value)
    }

    // changes color twice and makes sure that each change is different from the previous
    @Test
    fun multipleColorChanges() {
        val vm = testVM();

        val initialColor = vm.color.value

        vm.pickColor()
        val newColor1 = vm.color.value

        vm.pickColor()
        val newColor2 = vm.color.value

        assertNotEquals(initialColor, newColor1)
        assertNotEquals(newColor1, newColor2)
    }
}