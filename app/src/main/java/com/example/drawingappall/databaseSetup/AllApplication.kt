package com.example.drawingappall.databaseSetup

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Application class to hold global state, such as the Room database and repository.
 */
class AllApplication : Application() {

    // Application-wide coroutine scope for background operations
    val scope = CoroutineScope(SupervisorJob())

    // Lazily initialized Room database instance
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingsDatabase::class.java,
            "Drawings_database"
        )
            .fallbackToDestructiveMigration() // Destroys and recreates DB on schema mismatch
            .build()
    }

    // Lazily initialized repository using the DAO from the Room database
    val drawingsRepository by lazy {
        DrawingsRepository(scope, db.drawingsDao())
    }
}
