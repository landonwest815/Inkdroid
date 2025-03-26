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
 * Repository for managing drawing files and syncing with Room database.
 * Handles file I/O, persistence, and metadata via DAO.
 */
class DrawingsRepository(
    val scope: CoroutineScope,
    private val dao: DrawingsDAO
) {

    // Observes the list of drawing entries in the database
    val drawings: Flow<List<Drawing>> = dao.getAllDrawings()

    /**
     * Creates a new drawing entry in the database.
     */
    fun createFile(file: Drawing) {
        scope.launch {
            dao.createDrawing(file)
        }
    }

    /**
     * Saves the given [bitmap] to the specified [filePath] and [fileName].
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
     * Loads a drawing from disk.
     * Returns null if the file doesn't exist (e.g., when creating a new drawing).
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
     *
     * [onResult] is called with `true` if successful, `false` otherwise.
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
            onResult(false) // File doesn't exist
            return
        }

        scope.launch {
            val nameTaken = dao.fileNameExists(newName) > 0
            if (nameTaken) {
                onResult(false) // New name is already in use
                return@launch
            }

            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                val oldDrawing = dao.getDrawingByName(oldName)
                if (oldDrawing != null) {
                    val updatedDrawing = Drawing(newName, filePath).apply {
                        id = oldDrawing.id
                    }
                    dao.deleteDrawing(oldDrawing)
                    dao.createDrawing(updatedDrawing)
                }
                onResult(true)
            } else {
                onResult(false) // File rename failed
            }
        }
    }
}