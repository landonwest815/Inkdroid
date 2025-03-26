package com.example.drawingappall.databaseSetup

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Room database for storing drawing file metadata.
 */
@Database(entities = [Drawing::class], version = 2, exportSchema = false)
abstract class DrawingsDatabase : RoomDatabase() {
    abstract fun drawingsDao(): DrawingsDAO
}

/**
 * Data Access Object for performing operations on drawing files.
 */
@Dao
interface DrawingsDAO {

    // Inserts a new drawing file entry
    @Insert
    suspend fun createDrawing(data: Drawing)

    // Deletes an existing drawing file entry
    @Delete
    suspend fun deleteDrawing(data: Drawing)

    // Returns a flow of all drawing file entries
    @Query("SELECT * FROM drawing_files")
    fun getAllDrawings(): Flow<List<Drawing>>

    // Fetches a drawing by its filename
    @Query("SELECT * FROM drawing_files WHERE fileName = :fileName LIMIT 1")
    suspend fun getDrawingByName(fileName: String): Drawing?

    // Checks if a file with the given name exists
    @Query("SELECT COUNT(*) FROM drawing_files WHERE fileName = :fileName")
    suspend fun fileNameExists(fileName: String): Int
}