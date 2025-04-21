package com.example.drawingappall.databaseSetup

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Repository for managing drawing files and their metadata using Room.
 * Handles file I/O and database operations asynchronously via a coroutine scope.
 */
class DrawingsRepository(
    val scope: CoroutineScope,
    private val dao: DrawingsDAO
) {

    // A stream of all drawings in the database
    val drawings: Flow<List<Drawing>> = dao.getAllDrawings()

    /**
     * Inserts a new drawing into the database.
     */
    fun createFile(file: Drawing) {
        scope.launch {
            dao.createDrawing(file)
        }
    }

    /**
     * Saves the given [bitmap] to the file specified in [drawing].
     */
    fun saveDrawing(drawing: Drawing, bitmap: Bitmap) {
        saveDrawing(drawing.filePath, drawing.fileName, bitmap)
    }

    /**
     * Saves the given [bitmap] to a file at [filePath] with name [fileName].
     */
    fun saveDrawing(filePath: String, fileName: String, bitmap: Bitmap) {
        val path = File(filePath, fileName).absolutePath
        try {
            FileOutputStream(File(path)).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Loads a drawing from disk using the given [drawing] metadata.
     * Returns null if the file doesn't exist.
     */
    fun loadDrawing(drawing: Drawing): Bitmap? {
        return loadDrawing(drawing.filePath, drawing.fileName)
    }

    /**
     * Loads a drawing from disk using the provided [filePath] and [fileName].
     * Returns null if the file doesn't exist.
     */
    fun loadDrawing(filePath: String, fileName: String): Bitmap? {
        val file = File(filePath, fileName)
        if (!file.exists()) return null

        return try {
            FileInputStream(file.absolutePath).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Error loading drawing from file: ${file.absolutePath}", e)
        }
    }

    /**
     * Deletes the drawing file from disk and its corresponding entry from the database.
     */
    fun deleteDrawing(file: Drawing) {
        val fileToDelete = File(file.filePath, file.fileName)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }

        scope.launch {
            dao.deleteDrawing(file)
        }
    }

    /**
     * Renames a drawing's file on disk and updates the database record.
     * Calls [onResult] with true if successful, false otherwise.
     */
    fun renameDrawing(
        filePath: String,
        oldName: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        val oldFile = File(filePath, oldName)
        val newFile = File(filePath, newName)

        if (!oldFile.exists()) {
            onResult(false)
            return
        }

        scope.launch {
            val nameTaken = dao.fileNameExists(newName) > 0
            if (nameTaken) {
                onResult(false)
                return@launch
            }

            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                val drawing = dao.getDrawingByName(oldName)
                if (drawing != null) {
                    val updated = drawing.copy(fileName = newName)
                    dao.updateDrawing(updated)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Updates the [drawing]'s storage location in the database.
     */
    fun changeStorageLocation(drawing: Drawing, storageLocation: StorageLocation) {
        scope.launch {
            val updatedDrawing = drawing.copy(storageLocation = storageLocation)
            dao.updateDrawing(updatedDrawing)
        }
    }

    /**
     * Checks if a drawing with the given name does NOT exist in the database.
     * If it doesn't exist, [onResult] is called with the name.
     */
    fun doesNotContainDrawing(
        drawingName: String,
        onResult: (String) -> Unit
    ) {
        scope.launch {
            val nameTaken = dao.fileNameExists(drawingName) > 0
            if (!nameTaken) {
                onResult(drawingName)
            }
        }
    }
}
