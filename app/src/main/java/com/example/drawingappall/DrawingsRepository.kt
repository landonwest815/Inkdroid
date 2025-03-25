package com.example.drawingappall

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException



class DrawingsRepository(val scope: CoroutineScope, private val dao: DrawingsDAO) {

    val drawings: Flow<List<Drawing>> = dao.getAllDrawings()

    fun createFile( file :Drawing){
        scope.launch {
            dao.createDrawing(file)
        }
    }

    //Saves 'fileName' bitmap to the given file path
    fun saveDrawing(filePath : String, fileName : String, bitmap: Bitmap) {
        val path = File(filePath, fileName).absolutePath

        //Save the bitmap
        try {
            val fos: FileOutputStream = FileOutputStream(File(path))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close()

            //Log.d("SaveFile", "Saving to path: $path")
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //Loads a bitmap at the given file path and name
    fun loadDrawing(filePath : String, fileName : String) : Bitmap?
    {
        val file = File(filePath, fileName)

        //Normal if the user is creating a new file
        if (!file.exists())
        {
            return null
        }

        val path = file.absolutePath
        //Log.d("LoadFile", "Loading to path: $filePath")
        //Load the bitmap
        return try {
            FileInputStream(path).use { out ->
                BitmapFactory.decodeStream(out)
            }
        }
        //Failed to load bitmap
        catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Error loading drawing from file: $path", e)
        }
    }

    //Deletes a drawing's file and dao
    fun deleteDrawing(file: Drawing)
    {
        //Find and delete the file
        val fileToDelete = File(file.filePath, file.fileName)
        if (fileToDelete.exists())
            fileToDelete.delete()

        scope.launch {
            dao.deleteDrawing(file)
        }
    }
}