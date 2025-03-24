package com.example.drawingappall


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DrawingFileViewModel(private val repository: DrawingsRepository) : ViewModel() {

    val drawings: StateFlow<List<Drawing>> = repository.drawings.stateIn(
        scope = repository.scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = listOf() //start with an empty list
    )

    fun createFile(name: String): Drawing {
        //create blank bit map store in file
        val filePath = ""
        var file = Drawing(name, filePath)
        repository.createFile(file)
        return file
    }

}

object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        // if you had different VM classes, you could add an initailizer block for each one here
        initializer {
            DrawingFileViewModel(
                //fetches the application singleton
                (this[AndroidViewModelFactory.APPLICATION_KEY]
                        //and then extracts the repository in it
                        as AllApplication).DrawingsRepository
            )
        }
    }
}