package com.example.drawingappall

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AllApplication : Application(){
    //coroutine scope tied to the application lifetime which we can run suspend functions in
    val scope = CoroutineScope(SupervisorJob())

    //get a reference to the DB singleton
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingsDatabase::class.java,
            "Drawings_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    }

    //create our repository using lazy to access the DB when we need it
    val DrawingsRepository by lazy { DrawingsRepository(scope, db.drawingsDao()) }
}