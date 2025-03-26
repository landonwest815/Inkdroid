package com.example.drawingappall

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities= [Drawing::class], version = 2, exportSchema = false)
abstract class DrawingsDatabase : RoomDatabase(){
    abstract fun drawingsDao(): DrawingsDAO
}

@Dao
interface DrawingsDAO {

    @Insert
    suspend fun createDrawing(data: Drawing)

    @Delete
    suspend fun deleteDrawing(data: Drawing)

    @Query("SELECT * from drawing_files")
    fun getAllDrawings() : Flow<List<Drawing>>

    @Query("SELECT * FROM drawing_files WHERE fileName = :fileName LIMIT 1")
    suspend fun getDrawingByName(fileName: String): Drawing?

    @Query("SELECT COUNT(*) FROM drawing_files WHERE fileName = :fileName")
    suspend fun fileNameExists(fileName: String): Int

}