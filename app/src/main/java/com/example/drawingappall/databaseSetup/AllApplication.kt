package com.example.drawingappall.databaseSetup

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Main Application class to initialize Room database and repository.
 */
class AllApplication : Application() {

    /**
     * Scope for background operations.
     */
    val applicationScope = CoroutineScope(SupervisorJob())

    /**
     * Room database instance (lazily initialized).
     * Uses fallbackToDestructiveMigration for simplicity; consider proper migrations for production.
     */
    val database: DrawingsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingsDatabase::class.java,
            "drawing_database"
        )
            .fallbackToDestructiveMigration() // TODO: implement migrations
            .build()
    }

    /**
     * Repository for managing drawings (in-memory and on-disk).
     */
    val repository: DrawingsRepository by lazy {
        DrawingsRepository(applicationScope, database.drawingsDao())
    }
}
