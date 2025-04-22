package com.example.drawingappall.viewModels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.DrawingsRepository
import com.example.drawingappall.databaseSetup.StorageLocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

/**
 * ViewModel for managing drawing files: creation, deletion, renaming,
 * and exposing local vs. server-based drawing flows.
 */
class DrawingFileViewModel(
    private val repository: DrawingsRepository,
    private val context: Context
) : ViewModel() {

    // -------- Flows --------

    /** Only show drawings that live on disk (or were downloaded) AND belong to me */
    val localDrawings = repository.allDrawings
        .map { list ->
            val user = TokenStore.username
            list.filter { drawing ->
                // must be marked Local or Both
                (drawing.storageLocation == StorageLocation.Local
                        || drawing.storageLocation == StorageLocation.Both)
                        // must be mine
                        && drawing.ownerUsername == user
                        // **and** must actually exist on disk
                        && File(drawing.filePath, drawing.fileName).exists()
            }
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    // serverDrawings remains unchanged…
    val serverDrawings = repository.allDrawings
        .map { list ->
            list.filter {
                it.storageLocation == StorageLocation.Server
                        || it.storageLocation == StorageLocation.Both
            }
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    // -------- File Operations --------

    /**
     * Creates and saves a new blank drawing (800x800 PNG).
     * @param name filename to assign.
     * @return Drawing metadata for the new file.
     */
    fun createFile(name: String): Drawing {
        val dir = context.filesDir.absolutePath
        val drawing = Drawing(
            fileName = name,
            filePath = dir,
            storageLocation = StorageLocation.Local,
            ownerUsername = TokenStore.username
        )
        repository.create(drawing)

        // Initialize blank image
        val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        repository.saveToDisk(dir, name, bitmap)

        return drawing
    }

    /** Deletes the drawing from both disk and database. */
    fun deleteFile(drawing: Drawing) {
        val diskFile = File(drawing.filePath, drawing.fileName)
        if (drawing.storageLocation == StorageLocation.Both) {
            // user is simply removing their local copy
            if (diskFile.exists()) diskFile.delete()
            repository.updateLocation(drawing, StorageLocation.Server)
        } else {
            // truly-local file → delete DB + disk
            repository.delete(drawing)
        }
    }

    /**
     * Renames a drawing file and updates its database entry.
     * @param filePath directory of the drawing.
     * @param oldName current filename.
     * @param newName desired filename.
     * @param onResult callback invoked with success status.
     */
    fun renameFile(
        filePath: String,
        oldName: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        repository.rename(filePath, oldName, newName, onResult)
    }
}

/**
 * Factory for creating [DrawingFileViewModel] instances.
 */
object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            DrawingFileViewModel(
                app.repository,
                app.applicationContext
            )
        }
    }
}