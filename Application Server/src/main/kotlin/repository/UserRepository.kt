package com.application_server.repository

import com.application_server.model.User
import java.util.*

class UserRepository {

  private val users = mutableListOf<User>()

  fun findAll(): List<User> =
    users

  fun findById(id: UUID): User? =
    users.firstOrNull { it.id == id }

  fun findByUsername(username: String): User? {
    //println("Looking for user: $username")
    //println("Current users: ${users.map { it.username }}")
    return users.firstOrNull { it.username == username }
  }

  fun save(user: User): Boolean {
    //println("Saving user to in-memory list: ${user.username}")
    return users.add(user)
  }
}