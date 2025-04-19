package com.application_server.model

import java.util.UUID

data class User(
  val id: UUID,
  val username: String,
  val hashedPassword: String //bcrypt so it includes salt, etc
)