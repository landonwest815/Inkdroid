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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream

/**
 * ViewModel for handling user auth and syncing drawings with a Ktor backend.
 */
class SocialViewModel(
    private val repository: DrawingsRepository,
    private val context: Context
) : ViewModel() {

    companion object {
        // TODO: Move to BuildConfig
        private const val BASE_URL = "http://10.0.2.2:8080/api"
    }

    // --- Authentication state ---
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    // --- Ktor client for JSON + binary uploads/downloads ---
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { isLenient = true; ignoreUnknownKeys = true })
        }
    }

    init {
        // Already logged in if we have a non-blank JWT
        _isAuthenticated.value = !TokenStore.jwt.isNullOrBlank()
    }

    // === Authentication ===

    fun login(username: String, password: String) = viewModelScope.launch {
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

    fun register(username: String, password: String) = viewModelScope.launch {
        try {
            if (UserApi.register(username, password)) {
                login(username, password)
            } else {
                _authError.value = "Username already taken"
            }
        } catch (e: Exception) {
            _authError.value = "Registration failed: ${e.message}"
        }
    }

    fun logout() {
        TokenStore.jwt = null
        TokenStore.username = null
        _isAuthenticated.value = false
    }

    // === Upload a drawing ===

    /**
     * Loads a local bitmap, marks it shared locally, and POSTs it to the server.
     */
    fun uploadFile(drawing: Drawing) = viewModelScope.launch {
        // 1) Load from disk
        val bitmap = repository.loadFromDisk(drawing.filePath, drawing.fileName)
            ?: return@launch

        // 2) Mark as both local+server so UI shows “shared”
        repository.updateLocation(drawing, StorageLocation.Both)

        try {
            // 3) Compress to PNG bytes
            val pngBytes = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }.toByteArray()

            val user = TokenStore.username.orEmpty()
            val url = "$BASE_URL/upload/$user/${drawing.fileName}"

            // 4) Submit multipart POST
            val response: HttpResponse = client.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append(
                        key = "image",
                        value = pngBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${drawing.fileName}\""
                            )
                            append(
                                HttpHeaders.Authorization,
                                "Bearer ${TokenStore.jwt}"
                            )
                        }
                    )
                }
            )

            if (response.status == HttpStatusCode.OK) {
                Log.d("SocialViewModel", "Uploaded ${drawing.fileName}")
            } else {
                Log.e("SocialViewModel", "Upload failed: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("SocialViewModel", "Upload error: ${e.message}")
        }
    }

    // === Fetch & download new files ===

    /**
     * Retrieves remote filename list and downloads anything missing locally.
     */
    fun fetchFiles() = viewModelScope.launch {
        try {
            val resp = client.get("$BASE_URL/download/file_names")
            if (resp.status == HttpStatusCode.OK) {
                val text = resp.readBytes().toString(Charsets.UTF_8)
                val serverList = Json.parseToJsonElement(text)
                    .jsonArray
                    .map { it.jsonPrimitive.content }

                // Purge local records of drawings removed on server
                val allDrawings = repository.getAllDrawings() // suspend fun in repository returning List<Drawing>
                allDrawings
                    .filter { it.storageLocation != StorageLocation.Local }
                    .forEach { dr ->
                        val entry = "${dr.ownerUsername}/${dr.fileName}"
                        if (entry !in serverList) {
                            repository.delete(dr)
                        }
                    }

                serverList.forEach { entry ->
                    val (uploader, name) = parseFileName(entry)
                    // download if not already in DB
                    repository.ifNotExists(name) { missingName ->
                        downloadFile(uploader, missingName)
                    }
                }
            } else {
                Log.e("SocialViewModel", "Fetch failed: ${resp.status}")
            }
        } catch (e: Exception) {
            Log.e("SocialViewModel", "Fetch error: ${e.message}")
        }
    }

    /**
     * Downloads one PNG and saves it via Room + disk.
     */
    fun downloadFile(uploader: String, fileName: String) = viewModelScope.launch {
        try {
            val url = "$BASE_URL/download/$uploader/$fileName"
            val resp = client.get(url)
            if (resp.status == HttpStatusCode.OK) {
                val bytes = resp.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@launch

                // Save new drawing record + file, but mark it SERVER-only
                val path = context.filesDir.absolutePath
                val currentUser = TokenStore.username ?: return@launch
                val storage = if (uploader == currentUser) StorageLocation.Both
                else                       StorageLocation.Server

                val downloaded = Drawing(
                    fileName        = fileName,
                    filePath        = path,
                    storageLocation = storage,
                    ownerUsername   = uploader
                )
                repository.create(downloaded)
                repository.saveToDisk(path, fileName, bitmap)
            }
        } catch (e: Exception) { /* ... */ }
    }


    // === Delete remote ===

    /**
     * Sends DELETE to server, then removes locally if successful.
     */
    fun deleteRemote(drawing: Drawing) = viewModelScope.launch {
        val uploader = drawing.ownerUsername ?: return@launch
        val url = "$BASE_URL/download/$uploader/${drawing.fileName}"
        try {
            val resp = client.delete(url) {
                header(HttpHeaders.Authorization, "Bearer ${TokenStore.jwt}")
            }
            if (resp.status == HttpStatusCode.OK) {
                repository.delete(drawing)
                Log.d("SocialViewModel", "Removed remote ${drawing.fileName}")

                // only fetch *after* the delete has succeeded
                fetchFiles()
            } else {
                Log.e("SocialViewModel", "Remote delete failed: ${resp.status}")
            }
        } catch (e: Exception) {
            Log.e("SocialViewModel", "Remote delete error: ${e.message}")
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAllDrawings()  // your API call to wipe server data
        }
    }

    // === Helpers ===

    /** Splits "uploader/filename" into its parts. */
    private fun parseFileName(entry: String): Pair<String, String> {
        val parts = entry.split("/", limit = 2)
        return if (parts.size == 2) parts[0] to parts[1] else "unknown" to entry
    }
}

/**
 * Factory for creating [SocialViewModel] with necessary app deps.
 */
object SocialViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            SocialViewModel(app.repository, app.applicationContext)
        }
    }
}
