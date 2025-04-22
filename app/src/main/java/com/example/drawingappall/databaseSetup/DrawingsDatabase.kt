package com.example.drawingappall.databaseSetup

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Database holding the Drawing table.
 */
@Database(entities = [Drawing::class], version = 7, exportSchema = false)
abstract class DrawingsDatabase : RoomDatabase() {
    abstract fun drawingsDao(): DrawingsDao
}

/**
 * Data Access Object for CRUD operations on Drawing.
 */
@Dao
interface DrawingsDao {

    /** Inserts a new drawing record. */
    @Insert
    suspend fun insert(drawing: Drawing)

    /** Updates an existing drawing record. */
    @Update
    suspend fun update(drawing: Drawing)

    /** Deletes a drawing record. */
    @Delete
    suspend fun delete(drawing: Drawing)

    /**
     * Stream of all drawings in the database.
     * Emits updates whenever the data changes.
     */
    @Query("SELECT * FROM drawing_files")
    fun getAllDrawings(): Flow<List<Drawing>>

    /**
     * One‑off load of all drawings (for bulk operations).
     */
    @Query("SELECT * FROM drawing_files")
    suspend fun getAllDrawingsOnce(): List<Drawing>

    /**
     * Deletes every row in the drawing_files table.
     */
    @Query("DELETE FROM drawing_files")
    suspend fun deleteAllDrawings()

    /**
     * Finds a drawing by its fileName.
     * @return Drawing or null if not found
     */
    @Query("SELECT * FROM drawing_files WHERE fileName = :name LIMIT 1")
    suspend fun findByName(name: String): Drawing?

    /**
     * Counts existing drawings with the given fileName.
     * @return 0 if none, >0 if exists
     */
    @Query("SELECT COUNT(*) FROM drawing_files WHERE fileName = :name")
    suspend fun countByName(name: String): Int
}
