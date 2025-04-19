package com.application_server.plugins

import com.application_server.service.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(
  jwtService: JwtService
) {
  authentication {
    jwt {
      realm = jwtService.realm
      verifier(jwtService.jwtVerifier)

      validate { credential ->
        jwtService.customValidator(credential)
      }
    }

//    jwt("another-auth") {
//      realm = jwtService.realm
//      verifier(jwtService.jwtVerifier)
//
//      validate { credential ->
//        jwtService.customValidator(credential)
//      }
//    }
  }
}

