package com.example.drawingappall.databaseSetup

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room database class that holds the drawing files table.
 */
@Database(entities = [Drawing::class], version = 5, exportSchema = false)
abstract class DrawingsDatabase : RoomDatabase() {
    abstract fun drawingsDao(): DrawingsDAO
}

/**
 * Data Access Object for performing CRUD operations on Drawing entities.
 */
@Dao
interface DrawingsDAO {

    /**
     * Inserts a new drawing into the database.
     */
    @Insert
    suspend fun createDrawing(data: Drawing)

    /**
     * Deletes a drawing from the database.
     */
    @Delete
    suspend fun deleteDrawing(data: Drawing)

    /**
     * Returns a stream of all drawings in the database.
     */
    @Query("SELECT * FROM drawing_files")
    fun getAllDrawings(): Flow<List<Drawing>>

    /**
     * Retrieves a drawing by its file name.
     * @return the matching Drawing or null if not found.
     */
    @Query("SELECT * FROM drawing_files WHERE fileName = :fileName LIMIT 1")
    suspend fun getDrawingByName(fileName: String): Drawing?

    /**
     * Checks if a drawing with the given file name exists.
     * @return number of matches (0 or 1 expected).
     */
    @Query("SELECT COUNT(*) FROM drawing_files WHERE fileName = :fileName")
    suspend fun fileNameExists(fileName: String): Int

    /**
     * Updates an existing drawing entry.
     */
    @Update
    suspend fun updateDrawing(drawing: Drawing)
}
