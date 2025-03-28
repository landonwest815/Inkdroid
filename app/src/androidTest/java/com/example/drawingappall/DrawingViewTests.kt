package com.example.drawingappall

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.drawingappall.TestUtils.testDrawingFileVM
import com.example.drawingappall.TestUtils.testDrawingVM
import com.example.drawingappall.databaseSetup.Drawing
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class DrawingViewTests {

    @Test
    fun testSaveDrawing() {
        val vm = testDrawingVM()
        vm.drawOnCanvas(0f, 0f, 100, 100)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath = context.filesDir.absolutePath

        val drawing = Drawing(
            fileName = "drawing",
            filePath
        )

        try {
            vm.saveDrawing(drawing.filePath, drawing.fileName)
        }
        catch (e: Exception) {
            fail("Exception occurred while saving the drawing: ${e.message}")
        }

        val file = File(drawing.filePath, drawing.fileName)
        assertTrue(file.exists())
    }

    @Test
    fun testSaveEmptyDrawing() {
        val vm = testDrawingVM()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath = context.filesDir.absolutePath

        val drawing = Drawing(
            fileName = "drawing",
            filePath
        )

        try {
            vm.saveDrawing(drawing.filePath, drawing.fileName)
        }
        catch (e: Exception) {
            fail("Exception occurred while saving the drawing: ${e.message}")
        }

        val file = File(drawing.filePath, drawing.fileName)
        assertTrue(file.exists())
    }

    @Test
    fun testLoadDrawing() {
        val vm = testDrawingVM()
        vm.pickColor()
        vm.drawOnCanvas(0f, 0f, 100, 100)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath = context.filesDir.absolutePath

        val drawing = Drawing(
            fileName = "drawing",
            filePath
        )

        try {
            vm.saveDrawing(drawing.filePath, drawing.fileName)
        }
        catch (e: Exception) {
            fail("Exception occurred while saving the drawing: ${e.message}")
        }

        try {
            vm.loadDrawing(drawing.filePath, drawing.fileName)
        }
        catch (e: Exception) {
            fail("Exception occurred while loading the drawing: ${e.message}")
        }

        assertTrue(vm.color.value.red == vm.bitmap.value.getColor(0, 0,).red())
        assertTrue(vm.color.value.green == vm.bitmap.value.getColor(0, 0,).green())
        assertTrue(vm.color.value.blue == vm.bitmap.value.getColor(0, 0,).blue())
    }

    @Test
    fun testLoadUnsavedDrawing() {
        val vm = testDrawingVM()
        vm.drawOnCanvas(0f, 0f, 100, 100)

        val drawing = Drawing(
            fileName = "ghost",
            filePath = "/invalid/path/ghost.png"
        )

        try {
            vm.loadDrawing(drawing.filePath, drawing.fileName)
        }
        catch (e: Exception) {
            fail("Exception occurred while loading unknown drawing: ${e.message}")
        }
    }
}