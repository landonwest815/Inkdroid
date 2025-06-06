package com.example.drawingappall.databaseSetup

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved drawing file.
 * @param id autogenerated primary key
 * @param fileName name of the file (unique per user)
 * @param filePath directory path on disk
 * @param storageLocation where the image is stored (local/server/both)
 * @param ownerUsername username who created or uploaded this drawing
 */
@Entity(tableName = "drawing_files")
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val storageLocation: StorageLocation = StorageLocation.Local,
    val ownerUsername: String? = null
)

/**
 * Defines where a drawing is stored.
 */
enum class StorageLocation {
    Local,
    Server,
    Both
}

