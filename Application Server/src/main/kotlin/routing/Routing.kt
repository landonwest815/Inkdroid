package com.application_server.routing

import com.application_server.repository.NotesRepository
import com.application_server.service.JwtService
import com.application_server.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting(
  jwtService: JwtService,
  userService: UserService,
  notesRepository: NotesRepository
) {
  routing {

    route("/api/auth") {
      authRoute(jwtService)
    }

    route("/api/user") {
      userRoute(userService)
    }

    route("/api/hello") {
      helloRoute()
    }

    authenticate {
      route("/api/notes") {
        noteRoute(notesRepository)
      }

      route("/api/upload"){
        uploadRoute()
      }
    }
  }
}

fun extractPrincipalUsername(call: ApplicationCall): String? =
  call.principal<JWTPrincipal>()
    ?.payload
    ?.getClaim("username")
    ?.asString()



