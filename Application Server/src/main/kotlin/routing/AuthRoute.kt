package com.application_server.routing

import at.favre.lib.crypto.bcrypt.BCrypt
import com.all.auth.JwtConfig
import com.all.persistence.UserStore
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

  post("/register") {
    val request = call.receive<UserRequest>()
    val success = UserStore.register(request.username, request.password)

    if (success) {
      call.respond(HttpStatusCode.Created)
    } else {
      call.respond(HttpStatusCode.Conflict, "User already exists")
    }
  }

  post("/login") {
    val request = call.receive<LoginRequest>()
    val isValid = UserStore.validate(request.username, request.password)

    if (isValid) {
      val token = JwtConfig.generateToken(request.username)
      call.respond(mapOf("token" to token))
    } else {
      call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
    }
  }
}
