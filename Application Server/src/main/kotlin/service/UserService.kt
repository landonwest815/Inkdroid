package com.application_server.service

import com.application_server.model.User
import com.application_server.repository.UserRepository
import java.util.*

class UserService(
  private val userRepository: UserRepository
) {

  fun findAll(): List<User> =
    userRepository.findAll()

  fun findById(id: String): User? =
    userRepository.findById(
      id = UUID.fromString(id)
    )

  fun findByUsername(username: String): User? =
    userRepository.findByUsername(username)

  fun save(user: User): User? {
    val foundUser = userRepository.findByUsername(user.username)

    println("Saving user: ${user.username}")
    println("Found existing: $foundUser")

    return if (foundUser == null) {
      userRepository.save(user)
      user
    } else null
  }
}