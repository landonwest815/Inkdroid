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
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.accounts.UserApi
import com.example.drawingappall.databaseSetup.AllApplication
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.DrawingsRepository
import com.example.drawingappall.databaseSetup.StorageLocation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
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

/**
 * ViewModel for managing user authentication and syncing drawings with a remote server.
 */
class SocialViewModel(
    private val repository: DrawingsRepository,
    private val context: Context
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        TokenStore.jwt?.takeIf { it.isNotBlank() }?.let {
            _isAuthenticated.value = true
        }
    }

    /**
     * Attempts to log in the user with the provided credentials.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val token = UserApi.login(username, password)
                if (token != null) {
                    TokenStore.jwt = token
                    TokenStore.username = username
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

    /**
     * Attempts to register a new user and log them in on success.
     */
    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = UserApi.register(username, password)
                if (success) {
                    login(username, password)
                } else {
                    _authError.value = "Username already taken"
                }
            } catch (e: Exception) {
                _authError.value = "Registration failed: ${e.message}"
            }
        }
    }

    /**
     * Logs out the current user and clears session data.
     */
    fun logout() {
        TokenStore.jwt = null
        TokenStore.username = null
        _isAuthenticated.value = false
    }

    /**
     * Uploads the given drawing to the server.
     */
    fun uploadFile(file: Drawing) {
        val bitmap = repository.loadDrawing(file) ?: return
        repository.changeStorageLocation(file, StorageLocation.Both)

        repository.scope.launch {
            try {
                val baos = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                }
                val byteArray = baos.toByteArray()
                val username = TokenStore.username ?: "unknown"

                val response: HttpResponse = client.submitFormWithBinaryData(
                    url = "http://10.0.2.2:8080/api/upload/$username/${file.fileName}",
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
                    Log.d("upload", "✅ Uploaded: ${file.fileName}")
                } else {
                    Log.e("upload", "❌ Upload failed: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("upload", "❌ Error: ${e.message}")
            }
        }
    }

    /**
     * Fetches a list of all files on the server and downloads any new ones.
     */
    fun fetchFiles() {
        repository.scope.launch {
            try {
                val response: HttpResponse =
                    client.get("http://10.0.2.2:8080/api/download/file_names")
                if (response.status == HttpStatusCode.OK) {
                    val serverList: List<String> = response.body()
                    serverList.forEach { serverName ->
                        val (uploader, name) = parseFileName(serverName)
                        repository.doesNotContainDrawing(name) {
                            downloadFile(uploader, name)
                        }
                    }
                } else {
                    Log.e("fetch", "❌ Fetch failed: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("fetch", "❌ Error: ${e.message}")
            }
        }
    }

    /**
     * Downloads a drawing file from the server and saves it locally.
     */
    fun downloadFile(uploader: String, fileName: String) {
        repository.scope.launch {
            try {
                val url = "http://10.0.2.2:8080/api/download/$uploader/$fileName"
                val response = client.get(url)
                if (response.status == HttpStatusCode.OK) {
                    val bytes = response.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        val path = context.filesDir.absolutePath
                        val drawing = Drawing(
                            fileName = fileName,
                            filePath = path,
                            storageLocation = StorageLocation.Server,
                            ownerUsername = uploader
                        )
                        repository.createFile(drawing)
                        repository.saveDrawing(drawing, bitmap)
                    }
                } else {
                    Log.e("download", "❌ $url → ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("download", "❌ Exception: ${e.message}")
            }
        }
    }

    /** Sends DELETE /api/download/{uploader}/{filename} and, on success, deletes locally too */
    fun deleteRemote(file: Drawing) {
        val uploader = file.ownerUsername ?: return
        val name     = file.fileName

        repository.scope.launch {
            try {
                val response = client.delete("http://10.0.2.2:8080/api/download/$uploader/$name") {
                    header(HttpHeaders.Authorization, "Bearer ${TokenStore.jwt}")
                }
                if (response.status == HttpStatusCode.OK) {
                    // remove the DB & file
                    repository.deleteDrawing(file)
                    Log.d("deleteRemote", "Deleted $name on server")
                } else {
                    Log.e("deleteRemote","Server delete failed: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("deleteRemote","Exception: ${e.message}")
            }
        }
    }

    companion object {
        /**
         * Splits "uploader/filename" into its two parts.
         * E.g. "alice/sketch.png" → Pair("alice", "sketch.png")
         */
        private fun parseFileName(serverFileName: String): Pair<String, String> {
            val parts = serverFileName.split("/", limit = 2)
            return if (parts.size == 2) parts[0] to parts[1]
            else "unknown" to serverFileName
        }
    }
}

/**
 * ViewModel provider for creating [SocialViewModel] with required app dependencies.
 */
object SocialViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            SocialViewModel(app.drawingsRepository, app.applicationContext)
        }
    }
}
