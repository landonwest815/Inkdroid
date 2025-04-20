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

    // Uploads the specified drawing file
    fun uploadFile(file: Drawing) {
        val bitmap = repository.loadDrawing(file) ?: return

        // Show in both
        repository.changeStorageLocation(file, StorageLocation.Both)

        // Get Http Client
        val client = HttpClient(Android){
            install(ContentNegotiation){
                json(Json{
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val scope = repository.scope
        scope.launch {
            try{
                // Compress bitmap to bytes
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // Send the bitmap to the server
                val response: HttpResponse = client.submitFormWithBinaryData(
                    url = "http://10.0.2.2:8080/api/upload/${file.fileName}",
                    formData = formData {
                        append(
                            key = "image",
                            value = byteArray,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                            }
                        )
                    }
                )

                if (response.status == HttpStatusCode.OK) {
                    val responseData: String = response.body()
                    println("Response from server: $responseData")
                } else {
                    if (response.status.value in 200..299) {
                        println("Upload successful: ${response.status}")
                    } else {
                        println("Upload failed: ${response.status}")
                    }
                }
            } catch(e: Exception){
                Log.e("err", "${e.message}")
            }
        }
    }

    // Downloads the specified drawing file
    fun downloadFile(file: Drawing) {
        repository.changeStorageLocation(file, StorageLocation.Both)
    }

    // Gets all the drawings from the server
    fun fetchFiles() {
        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        println("Fetching Files")

        val scope = repository.scope
        scope.launch {
            try {
                val response: HttpResponse = client.get("http://10.0.2.2:8080/api/download/file_names")

                if (response.status == HttpStatusCode.OK) {
                    val fileNames: List<String> = response.body()

                    for (fileName in fileNames)
                    {
                        repository.doesNotContainDrawing(fileName) {
                            downloadFile(it, client)
                        }
                    }
                } else {
                    println("Fetch failed: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("err", "${e.message}")
            }
        }
    }

    private fun downloadFile(fileName: String, client: HttpClient) {
        val scope = repository.scope
        scope.launch {
            println("Downloading $fileName")

            // Download the file data
            val fileResponse: HttpResponse =
                client.get("http://10.0.2.2:8080/api/download/$fileName")
            if (fileResponse.status == HttpStatusCode.OK) {
                val byteArray = fileResponse.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                if (bitmap != null) {
                    // Add the image to the repository
                    val filePath = context.filesDir.absolutePath
                    val outputFile = Drawing(fileName, filePath, StorageLocation.Server)

                    repository.createFile(outputFile)
                    repository.saveDrawing(outputFile, bitmap)

                    println("Downloaded $fileName")
                } else {
                    println("Failed to download $fileName")
                }
            }
        }
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