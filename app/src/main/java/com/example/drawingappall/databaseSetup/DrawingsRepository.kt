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
 * Manages drawing files and metadata. Handles disk I/O and DB operations.
 */
class DrawingsRepository(
    val scope: CoroutineScope,
    private val dao: DrawingsDao
) {

    /** Stream of all drawings. */
    val allDrawings: Flow<List<Drawing>> = dao.getAllDrawings()

    // -------- Creation & Deletion --------

    /**
     * Enqueues insertion of a new Drawing record.
     */
    fun create(drawing: Drawing) {
        scope.launch { dao.insert(drawing) }
    }

    /**
     * Deletes file from disk and its record.
     */
    fun delete(drawing: Drawing) {
        val file = File(drawing.filePath, drawing.fileName)
        if (file.exists()) file.delete()
        scope.launch { dao.delete(drawing) }
    }

    // -------- Save & Load --------

    /**
     * Writes [bitmap] to disk at [filePath]/[fileName].
     */
    fun saveToDisk(filePath: String, fileName: String, bitmap: Bitmap) {
        val target = File(filePath, fileName)
        try {
            FileOutputStream(target).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Loads a Bitmap from disk; returns null if missing.
     */
    fun loadFromDisk(filePath: String, fileName: String): Bitmap? {
        val file = File(filePath, fileName)
        if (!file.exists()) return null
        return try {
            FileInputStream(file).use { BitmapFactory.decodeStream(it) }
        } catch (e: IOException) {
            throw RuntimeException("Failed to load drawing: ${file.absolutePath}", e)
        }
    }

    // -------- Rename & Storage Location --------

    /**
     * Renames a drawing on disk and updates its DB record.
     * Calls [onResult] with success status.
     */
    fun rename(
        dirPath: String,
        oldName: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        val oldFile = File(dirPath, oldName)
        val newFile = File(dirPath, newName)
        if (!oldFile.exists()) { onResult(false); return }
        scope.launch {
            if (dao.countByName(newName) > 0) { onResult(false); return@launch }
            val ok = oldFile.renameTo(newFile)
            if (ok) dao.findByName(oldName)?.let {
                dao.update(it.copy(fileName = newName))
                onResult(true)
            } else onResult(false)
        }
    }

    /** Updates the storage location flag in DB. */
    fun updateLocation(drawing: Drawing, location: StorageLocation) {
        scope.launch { dao.update(drawing.copy(storageLocation = location)) }
    }

    // -------- Existence Check --------

    /**
     * Executes [onMissing] if no drawing exists under [name].
     */
    fun ifNotExists(name: String, onMissing: (String) -> Unit) {
        scope.launch {
            if (dao.countByName(name) == 0) onMissing(name)
        }
    }

    /**
     * Returns a snapshot list of all drawings from the database.
     */
    suspend fun getAllDrawings(): List<Drawing> =
        dao.getAllDrawingsOnce()


    /**
     * Deletes *every* drawing record and its PNG file from disk.
     */
    fun deleteAllDrawings() {
        scope.launch {
            val drawings = dao.getAllDrawingsOnce()
            drawings.forEach { File(it.filePath, it.fileName).delete() }
            dao.deleteAllDrawings()
        }
    }
}
