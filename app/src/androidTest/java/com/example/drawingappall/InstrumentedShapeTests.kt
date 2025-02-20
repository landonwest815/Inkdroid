package com.example.drawingappall

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedShapeTests {
    private fun testChangeSize(size : Float){
        val vm = DrawingViewModel()
        val before = vm.shapeSize.value

        var callbackFired = false

        runBlocking {
            launch {
                vm.shapeSize.take(1).collect {
                    callbackFired = true
                }
            }
            vm.updateSize(size)
        }

        assertTrue(callbackFired)
        assertEquals(size, vm.shapeSize.value)
    }

    private fun testDrawShape(x : Float, y : Float, viewWidth : Int, viewHeight : Int, brushShape : BrushShape){
        val vm = DrawingViewModel()
        val before = vm.bitmap.value

        var callbackShapeFired = false
        var callbackDrawFired = false

        runBlocking {
            launch {
                vm.brushShape.take(1).collect {
                    callbackShapeFired = true
                }
            }
            vm.changeShape(brushShape)
        }

        runBlocking {
            launch {
                vm.bitmap.take(1).collect {
                    callbackDrawFired = true
                }
            }
            vm.drawOnCanvas(x, y, viewWidth, viewHeight)
        }

        assertTrue(callbackShapeFired)
        assertTrue(callbackDrawFired)

        assertNotSame(before, vm.bitmap.value)
    }

    @Test
    fun `test draw basic shapes`() {
        testChangeSize(25f)

        // Try drawing each shape
        testDrawShape(10f, 10f, 100, 100, BrushShape.Square)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Triangle)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Circle)
    }

    @Test
    fun `test draw smallest shapes`() {
        // Create small shapes
        testChangeSize(5f)

        // Try drawing each shape
        testDrawShape(10f, 10f, 100, 100, BrushShape.Square)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Triangle)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Circle)
    }

    @Test
    fun `test draw largest shapes`() {
        // Create large shapes
        testChangeSize(100f)

        // Try drawing each shape
        testDrawShape(10f, 10f, 100, 100, BrushShape.Square)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Triangle)
        testDrawShape(10f, 10f, 100, 100, BrushShape.Circle)
    }

    @Test
    fun `test draw outside bounds`() {
        testChangeSize(25f)

        // Make sure no errors occur (should draw outside bounds)
        testDrawShape(-9999f, -9999f, 100, 100, BrushShape.Square)
        testDrawShape(9999f, -9999f, 100, 100, BrushShape.Triangle)
        testDrawShape(9999f, -9999f, 100, 100, BrushShape.Circle)
    }

    @Test
    fun `test draw with invalid view`() {
        testChangeSize(25f)

        // Make sure no errors occur (should draw outside bounds)
        testDrawShape(10f, 10f, -9999, -9999, BrushShape.Square)
        testDrawShape(10f, 10f, -9999, -9999, BrushShape.Triangle)
        testDrawShape(10f, 10f, -9999, -9999, BrushShape.Circle)
    }
}