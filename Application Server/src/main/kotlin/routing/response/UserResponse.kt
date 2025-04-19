package com.application_server.routing.response

import com.application_server.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserResponse(
  @Serializable(with = UUIDSerializer::class)
  val id: UUID,
  val username: String,
)