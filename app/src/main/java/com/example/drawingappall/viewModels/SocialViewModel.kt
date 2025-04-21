package com.example.drawingappall.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.DrawingsRepository
import com.example.drawingappall.databaseSetup.StorageLocation
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.accounts.UserApi
import com.example.drawingappall.databaseSetup.AllApplication
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class SocialViewModel(
    private val repository: DrawingsRepository,
    private val context: Context
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    init {
        // 🚀 Auto-login if token already saved
        val token = TokenStore.jwt
        if (!token.isNullOrBlank()) {
            _isAuthenticated.value = true
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val token = UserApi.login(username, password)
                if (token != null) {
                    TokenStore.jwt = token
                    _isAuthenticated.value = true
                    _authError.value = null
                } else {
                    _authError.value = "Invalid credentials"
                }
            } catch (e: Exception) {
                _authError.value = "Login failed: ${e.message}"
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = UserApi.register(username, password)
                if (success) {
                    login(username, password) // ✅ Automatically log in after registration
                } else {
                    _authError.value = "Username already taken"
                }
            } catch (e: Exception) {
                _authError.value = "Registration failed: ${e.message}"
            }
        }
    }

    // 🌐 Upload drawing to server
    fun uploadFile(file: Drawing) {
        val bitmap = repository.loadDrawing(file) ?: return

        repository.changeStorageLocation(file, StorageLocation.Both)

        repository.scope.launch {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                val response: HttpResponse = client.submitFormWithBinaryData(
                    url = "http://10.0.2.2:8080/api/upload/${file.fileName}",
                    formData = formData {
                        append(
                            key = "image",
                            value = byteArray,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                                append(HttpHeaders.Authorization, "Bearer ${TokenStore.jwt}")
                            }
                        )
                    }
                )

                if (response.status == HttpStatusCode.OK) {
                    val responseData: String = response.body()
                    println("Response from server: $responseData")
                } else {
                    println("Upload failed: ${response.status}")
                }

            } catch (e: Exception) {
                Log.e("upload", "Error: ${e.message}")
            }
        }
    }

    // 🌐 Fetch file names from server
    fun fetchFiles() {
        repository.scope.launch {
            try {
                val response: HttpResponse = client.get("http://10.0.2.2:8080/api/download/file_names")

                if (response.status == HttpStatusCode.OK) {
                    val fileNames: List<String> = response.body()
                    for (fileName in fileNames) {
                        repository.doesNotContainDrawing(fileName) {
                            downloadFile(it)
                        }
                    }
                } else {
                    println("Fetch failed: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("fetch", "Error: ${e.message}")
            }
        }
    }

    // 🌐 Download a drawing file
    fun downloadFile(fileName: String) {
        repository.scope.launch {
            val response = client.get("http://10.0.2.2:8080/api/download/$fileName")

            if (response.status == HttpStatusCode.OK) {
                val byteArray = response.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                if (bitmap != null) {
                    val filePath = context.filesDir.absolutePath
                    val outputFile = Drawing(fileName, filePath, StorageLocation.Server)

                    repository.createFile(outputFile)
                    repository.saveDrawing(outputFile, bitmap)
                    println("Downloaded $fileName")
                } else {
                    println("Failed to decode $fileName")
                }
            }
        }
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    fun logout() {
        TokenStore.jwt = null
        _isAuthenticated.value = false
    }
}

// 👇 ViewModel factory for Compose
object SocialViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            val drawingsRepository = application.drawingsRepository
            val context = application.applicationContext

            SocialViewModel(drawingsRepository, context)
        }
    }
}
