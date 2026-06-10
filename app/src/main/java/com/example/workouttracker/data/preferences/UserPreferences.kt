package com.example.workouttracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
}
