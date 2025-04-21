package com.application_server.routing

import at.favre.lib.crypto.bcrypt.BCrypt
import com.application_server.model.User
import com.application_server.routing.request.LoginRequest
import com.application_server.routing.response.UserResponse
import com.application_server.service.JwtService
import com.application_server.service.UserService
import com.ktor.routing.request.UserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.UUID

fun Route.authRoute(jwtService: JwtService, userService: UserService) {

  post("/login") {
    val loginRequest = call.receive<LoginRequest>()

    val token: String? = jwtService.createJwtToken(loginRequest)

    token?.let {
      call.respond(hashMapOf("token" to token))
    } ?: call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
  }

  post("/register") {
    println("✅ HIT /api/auth/register")

    try {
      // STEP 2: log raw body
      val raw = call.receiveText()
     // println("🔍 Raw body: $raw")

      // manually decode JSON string to UserRequest
      val request = Json.decodeFromString<UserRequest>(raw)
      //println("Parsed request: ${request.username}")

      val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
      val newUser = User(
        id = UUID.randomUUID(),
        username = request.username,
        hashedPassword = hashedPassword
      )

      val saved = userService.save(newUser)
      println("Saved user: ${saved != null}")

      if (saved != null) {
        call.respond(HttpStatusCode.Created)
      } else {
        call.respond(HttpStatusCode.Conflict, "Username already exists")
      }
    } catch (e: Exception) {
      println("❌ Exception while handling /register: ${e.message}")
      call.respond(HttpStatusCode.BadRequest, "Malformed request")
    }
  }
}
