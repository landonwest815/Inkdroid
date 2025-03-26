package com.example.drawingappall

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingappall.TestUtils.testDrawingFileVM
import com.example.drawingappall.databaseSetup.Drawing
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DrawingFileTests {

    @Test
    fun testCreateFile() {
        val vm = testDrawingFileVM()
        val fileName = "test_drawing"

        val drawing = vm.createFile(fileName)

        assertEquals(fileName, drawing.fileName)
        assertTrue(drawing.filePath.contains("files"))
    }

    @Test
    fun testDeleteFile() {
        val vm = testDrawingFileVM()
        val file = vm.createFile("delete_test")

        vm.deleteFile(file)

        // Confirm it's no longer in the drawings list (give it a moment to update)
        runBlocking {
            delay(1000)
            val stillExists = vm.drawings.value.any { it.fileName == file.fileName }
            assertFalse(stillExists)
        }
    }

    @Test
    fun testRenameFileSuccess() = runBlocking {
        val vm = testDrawingFileVM()
        val oldName = "old_name"
        val newName = "new_name"

        val file = vm.createFile(oldName)

        var result = false
        vm.renameDrawing(file.filePath, oldName, newName) {
            result = it
        }

        delay(500)
        assertTrue(result)
    }

    @Test
    fun testRenameFileFailure() = runBlocking {
        val vm = testDrawingFileVM()

        var result = true
        vm.renameDrawing("/fake/path", "doesnt_exist", "new_name") {
            result = it
        }

        delay(500)
        assertFalse(result)
    }

    @Test
    fun testCreateFile_emptyName_failsGracefully() {
        val vm = testDrawingFileVM()
        val fileName = ""

        val drawing = vm.createFile(fileName)

        // You might still get a file, but we want to ensure it's not accepted
        assertTrue(drawing.fileName.isBlank() || drawing.fileName == "")
    }

    // test file renaming to existing name

    @Test
    fun testRenameToExistingNameFails() = runBlocking {
        val vm = testDrawingFileVM()

        vm.createFile("existing_name")
        val file = vm.createFile("original")

        var result = true
        vm.renameDrawing(file.filePath, "original", "existing_name") {
            result = it
        }

        delay(500)
        assertFalse(result)
    }

    @Test
    fun testPhysicalFileExists() {
        val vm = testDrawingFileVM()
        val drawing = vm.createFile("disk_check")

        val file = java.io.File(drawing.filePath)
        assertTrue(file.exists())
    }

    @Test
    fun testDeleteNonExistentFileDoesNotCrash() {
        val vm = testDrawingFileVM()
        val fakeDrawing = Drawing(
            fileName = "ghost",
            filePath = "/invalid/path/ghost.png"
        )

        // Should fail silently or safely
        vm.deleteFile(fakeDrawing)
    }
}