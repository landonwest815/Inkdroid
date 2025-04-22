package com.all.persistence

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

object UserStore {
    private val userFile = File("users.json")
    private val users = mutableMapOf<String, String>() // username to password

    init {
        if (userFile.exists()) {
            val json = userFile.readText()
            val loaded = Json.decodeFromString<Map<String, String>>(json)
            users.putAll(loaded)
        }
    }

    fun register(username: String, password: String): Boolean {
        if (users.containsKey(username)) return false
        users[username] = password
        save()
        return true
    }

    fun validate(username: String, password: String): Boolean {
        return users[username] == password
    }

    private fun save() {
        val json = Json.encodeToString(users)
        userFile.writeText(json)
    }
}
