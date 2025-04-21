package com.all.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "very_secret_jwt_key" // 🔒 Replace in production!
    private const val issuer = "drawALL"
    private const val validityInMs = 36_000_00 * 24 // 24 hours

    private val algorithm = Algorithm.HMAC512(secret)

    fun generateToken(username: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    fun verifyToken(token: String) = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()
        .verify(token)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}