package com.example.drawingappall.viewModels


import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.DrawingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DrawingFileViewModel(private val repository: DrawingsRepository, private val context : Context) : ViewModel() {

    // Creates a new file and saves an empty bitmap into it
    fun createFile(name: String): Drawing {
        val filePath = context.filesDir.absolutePath
        val file = Drawing(name, filePath)

        repository.createFile(file)

        val emptyBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        repository.saveDrawing(filePath, name, emptyBitmap)

        return file
    }

    // Deletes the specified drawing file
    fun deleteFile(file: Drawing) {
        repository.deleteDrawing(file)
    }

    // Renames a drawing file and passes result (success/failure) to callback
    fun renameDrawing(
        filePath: String,
        oldName: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        repository.renameDrawing(filePath, oldName, newName, onResult)
    }

    // Observe the list of drawings as a StateFlow
    val drawings: StateFlow<List<Drawing>> = repository.drawings.stateIn(
        scope = repository.scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList() // start with an empty list
    )

    // Observe the list of drawings as a StateFlow
    val uploadedDrawings: StateFlow<List<Drawing>> = repository.drawings.stateIn(
        scope = repository.scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList() // start with an empty list
    )

    // Uploads the specified drawing file
    fun uploadFile(file: Drawing) {

    }

    // Downloads the specified drawing file
    fun downloadFile(file: Drawing) {

    }
}

object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            val drawingsRepository = application.drawingsRepository
            val context = application.applicationContext

            DrawingFileViewModel(drawingsRepository, context)
        }
    }
}