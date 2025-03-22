package com.example.drawingappall

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "DrawingFiles")
data class Drawing(var fileName: String, var filePath: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}