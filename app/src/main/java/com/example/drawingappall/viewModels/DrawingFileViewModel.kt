package com.example.drawingappall.viewModels


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.DrawingsRepository
import com.example.drawingappall.databaseSetup.StorageLocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

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
    val drawings: StateFlow<List<Drawing>> = repository.drawings
        .map { allDrawings ->
            val filtered = allDrawings.filter { it.storageLocation == StorageLocation.Local
                    || it.storageLocation == StorageLocation.Both }
            println("Filtered drawings: ${filtered.map { it.storageLocation }}")
            filtered
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )


    // Observe the list of server drawings as a StateFlow
    val serverDrawings: StateFlow<List<Drawing>> = repository.drawings
        // Sort by StorageLocation
        .map { allDrawings ->
            allDrawings.filter { it.storageLocation == StorageLocation.Server ||
                    it.storageLocation == StorageLocation.Both }
        }
        .stateIn(
            scope = repository.scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

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