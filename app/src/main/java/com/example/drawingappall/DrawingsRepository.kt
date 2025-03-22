package com.example.drawingappall

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DrawingsRepository(val scope: CoroutineScope,
                         private val dao: DrawingsDAO) {
    val drawings = dao.drawings()

    fun createFile( file :Drawing){
        scope.launch {
            dao.createDrawing(file)
        }
    }
}