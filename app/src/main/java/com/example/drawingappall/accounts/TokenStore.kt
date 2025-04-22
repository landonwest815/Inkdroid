package com.example.drawingappall.accounts

/**
 * Singleton for holding session state: JWT token and current username.
 * Values reset when the app restarts (not persisted).
 */
object TokenStore {
    /** JWT token for authenticated API calls */
    var jwt: String? = null

    /** Username of the currently logged-in user */
    var username: String? = null
}