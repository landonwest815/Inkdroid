package com.example.drawingappall.databaseSetup

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a drawing file saved on disk.
 *
 * @property id Unique identifier for the drawing (auto-generated).
 * @property fileName The name of the drawing file.
 * @property filePath The absolute path to the file.
 * @property storageLocation Indicates if the file is stored locally, on the server, or both.
 * @property ownerUsername Username of the user who created/uploaded the drawing (nullable).
 */
@Entity(tableName = "drawing_files")
data class Drawing(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val fileName: String,
    val filePath: String,
    val storageLocation: StorageLocation,
    val ownerUsername: String? = null
)

/**
 * Enum class indicating where a drawing is stored.
 */
enum class StorageLocation {
    Local,
    Server,
    Both
}
