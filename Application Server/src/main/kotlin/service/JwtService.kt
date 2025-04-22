package com.application_server.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.application_server.model.User
import com.application_server.routing.request.LoginRequest
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*
import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.util.*
import kotlin.text.toCharArray

class JwtService(
  private val application: Application,
  private val userService: UserService,
) {

  private val secret = getConfigProperty("jwt.secret")
  private val issuer = getConfigProperty("jwt.issuer")
  private val audience = getConfigProperty("jwt.audience")

  val realm = getConfigProperty("jwt.realm")

  val jwtVerifier: JWTVerifier =
    JWT
      .require(Algorithm.HMAC256(secret))
      .withAudience(audience)
      .withIssuer(issuer)
      .build()

  fun createJwtToken(loginRequest: LoginRequest): String? {
    val foundUser: User? = userService.findByUsername(loginRequest.username)

    return if (foundUser != null &&
      BCrypt.verifyer().verify(loginRequest.password.toCharArray(),
        foundUser.hashedPassword).verified)
      JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", loginRequest.username)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
        .sign(Algorithm.HMAC256(secret))
    else
      null
  }

  fun customValidator(
    credential: JWTCredential,
  ): JWTPrincipal? {
    val username: String? = extractUsername(credential)
    val foundUser: User? = username?.let(userService::findByUsername)

    return foundUser?.let {
      if (audienceMatches(credential))
        JWTPrincipal(credential.payload)
      else
        null
    }
  }

  private fun audienceMatches(
    credential: JWTCredential,
  ): Boolean =
    credential.payload.audience.contains(audience)

  private fun getConfigProperty(path: String): String {
    println("Looking up config path: $path")
    return application.environment.config.property(path).getString()
  }


  private fun extractUsername(credential: JWTCredential): String? =
    credential.payload.getClaim("username").asString()
}


