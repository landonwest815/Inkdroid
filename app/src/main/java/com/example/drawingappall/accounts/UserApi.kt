package com.example.drawingappall.accounts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

/**
 * HTTP client for user authentication endpoints.
 * Uses Ktor with OkHttp engine and JSON serialization.
 */
object UserApi {
    // TODO: Move BASE_URL to a centralized config or BuildConfig
    private const val BASE_URL = "http://10.0.2.2:8080/api/auth"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json() }
        install(Logging) { level = LogLevel.BODY }
    }

    /**
     * Register a new user.
     * @param username desired username
     * @param password desired password
     * @return true if server returned HTTP 201 Created
     */
    suspend fun register(username: String, password: String): Boolean {
        val response = client.post("$BASE_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest(username, password))
        }
        return response.status == HttpStatusCode.Created
    }

    /**
     * Log in an existing user.
     * @param username user identifier
     * @param password user secret
     * @return JWT token on success, or null on failure
     */
    suspend fun login(username: String, password: String): String? {
        val response = client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        return if (response.status == HttpStatusCode.OK) {
            response.body<Map<String, String>>()
                .getOrDefault("token", null)
        } else {
            null
        }
    }

    @Serializable
    private data class UserRequest(val username: String, val password: String)

    @Serializable
    private data class LoginRequest(val username: String, val password: String)
}
