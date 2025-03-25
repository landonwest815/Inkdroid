package com.example.drawingappall


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DrawingFileViewModel(private val repository: DrawingsRepository, private val context : Context) : ViewModel() {

    val drawings: StateFlow<List<Drawing>> = repository.drawings.stateIn(
        scope = repository.scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = listOf() //start with an empty list
    )

    //Creates a file
    fun createFile(name: String): Drawing {
        //create blank bit map store in file
        val filePath = context.filesDir.absolutePath
        var file = Drawing(name, filePath)
        repository.createFile(file)
        return file
    }

    //Deletes a file
    fun deleteFile(file : Drawing)
    {
        repository.deleteDrawing(file)
    }
}

object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        // if you had different VM classes, you could add an initailizer block for each one here
        initializer {
            val application = this[AndroidViewModelFactory.APPLICATION_KEY] as AllApplication
            val drawingsRepository = application.DrawingsRepository
            val context = application.applicationContext

            DrawingFileViewModel(drawingsRepository, context)
        }
    }
}