package com.example.drawingappall.accounts

/**
 * A singleton object for storing the current session's JWT and username in memory.
 * These values are not persisted and will reset on app restart.
 */
object TokenStore {
    var jwt: String? = null
    var username: String? = null
}