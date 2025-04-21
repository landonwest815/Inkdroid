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

/**
 * ViewModel for managing drawing files and syncing local vs. server-based entries.
 */
class DrawingFileViewModel(
    private val repository: DrawingsRepository,
    private val context: Context
) : ViewModel() {

    /**
     * Creates a new drawing file with an empty 800x800 bitmap and stores it locally.
     * @return the [Drawing] entry that was created.
     */
    fun createFile(name: String): Drawing {
        val filePath = context.filesDir.absolutePath
        val file = Drawing(
            fileName = name,
            filePath = filePath,
            storageLocation = StorageLocation.Local,
            ownerUsername = TokenStore.username
        )

        repository.createFile(file)

        val emptyBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        repository.saveDrawing(filePath, name, emptyBitmap)

        return file
    }

    /**
     * Deletes the specified [file] from both disk and database.
     */
    fun deleteFile(file: Drawing) {
        repository.deleteDrawing(file)
    }

    /**
     * Attempts to rename the given file and update its database record.
     * @param onResult Callback with `true` if successful, `false` otherwise.
     */
    fun renameDrawing(
        filePath: String,
        oldName: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        repository.renameDrawing(filePath, oldName, newName, onResult)
    }

    /**
     * Flow of local drawings owned by the currently logged-in user.
     */
    val drawings: StateFlow<List<Drawing>> = repository.drawings
        .map { allDrawings ->
            val currentUser = TokenStore.username
            allDrawings.filter {
                (it.storageLocation == StorageLocation.Local || it.storageLocation == StorageLocation.Both) &&
                        it.ownerUsername == currentUser
            }
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    /**
     * Flow of drawings stored on the server (or both server and local).
     */
    val serverDrawings: StateFlow<List<Drawing>> = repository.drawings
        .map { allDrawings ->
            allDrawings.filter {
                it.storageLocation == StorageLocation.Server || it.storageLocation == StorageLocation.Both
            }
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
}

/**
 * Provides a factory for instantiating [DrawingFileViewModel] with application dependencies.
 */
object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            DrawingFileViewModel(
                application.drawingsRepository,
                application.applicationContext
            )
        }
    }
}
