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

object UserApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    private const val BASE_URL = "http://10.0.2.2:8080/api/auth"

    suspend fun register(username: String, password: String): Boolean {
        val response = client.post("$BASE_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest(username, password))
        }

        return response.status == HttpStatusCode.Created
    }

    suspend fun login(username: String, password: String): String? {
        val response = client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }

        return (if (response.status == HttpStatusCode.OK) {
            response.body<Map<String, String>>()["token"]
        } else null).toString()
    }

    @Serializable
    data class UserRequest(val username: String, val password: String)

    @Serializable
    data class LoginRequest(val username: String, val password: String)
}
