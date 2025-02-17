package com.example.customviewdemo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SizeViewModel : ViewModel() {

    private val circleSize_ = MutableStateFlow(0.25f)
    val circleSize = circleSize_ as StateFlow<Float>

    fun update(newSize: Float){
        circleSize_.value = newSize
    }

}