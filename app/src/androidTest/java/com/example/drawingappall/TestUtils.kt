package com.example.drawingappall

import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object TestUtils {
    fun testVM(): DrawingViewModel {
        val context = ApplicationProvider.getApplicationContext<Context>()

        //coroutine scope tied to the application lifetime which we can run suspend functions in
        val scope = CoroutineScope(SupervisorJob())

        //get a reference to the DB singleton
        val db by lazy {
            Room.databaseBuilder(
                context,
                DrawingsDatabase::class.java,
                "Drawings_database"
            ).build()
        }

        val fakeRepository = DrawingsRepository(scope, db.drawingsDao())
        return DrawingViewModel(fakeRepository)
    }
}