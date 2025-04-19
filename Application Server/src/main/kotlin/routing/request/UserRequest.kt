package com.ktor.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
  val username: String,
  val password: String,
)