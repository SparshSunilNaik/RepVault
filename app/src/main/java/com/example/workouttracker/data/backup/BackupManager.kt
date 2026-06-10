package com.example.workouttracker.data.backup

import android.content.Context
import android.net.Uri
import com.example.workouttracker.data.repository.WorkoutRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

class BackupManager(private val repository: WorkoutRepository) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(): String {
        val data = repository.exportAllData()
        return gson.toJson(data)
    }

    suspend fun importFromJson(json: String): Boolean {
        return try {
            val data = gson.fromJson(json, WorkoutRepository.BackupData::class.java)
            repository.importData(data)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportToUri(context: Context, uri: Uri): Boolean {
        return try {
            val json = exportToJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            reader.close()
            importFromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
