package com.application_server.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
  val username: String,
  val password: String,
)