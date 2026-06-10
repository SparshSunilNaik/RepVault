package com.example.workouttracker

import android.app.Application
import com.example.workouttracker.data.backup.BackupManager
import com.example.workouttracker.data.db.AppDatabase
import com.example.workouttracker.data.preferences.UserPreferences
import com.example.workouttracker.data.preferences.dataStore
import com.example.workouttracker.data.repository.WorkoutRepository

class RepVaultApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: WorkoutRepository by lazy {
        WorkoutRepository(database)
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this.dataStore)
    }

    val backupManager: BackupManager by lazy {
        BackupManager(repository)
    }
}
