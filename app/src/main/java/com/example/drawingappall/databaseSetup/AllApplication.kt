package com.example.drawingappall.databaseSetup

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Custom Application class for global app-level dependencies.
 */
class AllApplication : Application() {

    // Coroutine scope tied to the application's lifetime
    val scope = CoroutineScope(SupervisorJob())

    // Lazily initialized reference to the Room database singleton
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingsDatabase::class.java,
            "Drawings_database"
        )
            .fallbackToDestructiveMigration() // rebuilds DB if schema changes
            .build()
    }

    // Lazily initialized repository, tied to the app's database and scope
    val drawingsRepository by lazy {
        DrawingsRepository(scope, db.drawingsDao())
    }
}