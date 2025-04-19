package com.application_server.routing

import com.application_server.routing.request.LoginRequest
import com.application_server.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(jwtService: JwtService) {

  post {
    val loginRequest = call.receive<LoginRequest>()

    val token: String? = jwtService.createJwtToken(loginRequest)

    token?.let {
      call.respond(hashMapOf("token" to token))
    } ?: call.respond(
      message = HttpStatusCode.Unauthorized
    )
  }

}