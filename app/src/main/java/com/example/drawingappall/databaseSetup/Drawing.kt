package com.example.drawingappall.databaseSetup

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a drawing file stored on disk.
 */
@Entity(tableName = "drawing_files")
data class Drawing(
    var fileName: String,
    var filePath: String,
    var storageLocation: StorageLocation = StorageLocation.Local,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

enum class StorageLocation
{
    Local,
    Server,
    Both
}