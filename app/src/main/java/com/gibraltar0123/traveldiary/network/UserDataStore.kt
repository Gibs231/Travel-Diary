package com.gibraltar0123.traveldiary.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gibraltar0123.traveldiary.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserDataStore(private val context: Context) {

    companion object {
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val PHOTO_URL_KEY = stringPreferencesKey("user_photo_url")
    }


    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            name = preferences[NAME_KEY] ?: "",
            email = preferences[EMAIL_KEY] ?: "",
            photoUrl = preferences[PHOTO_URL_KEY] ?: ""
        )
    }


    suspend fun saveData(user: User) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = user.name
            preferences[EMAIL_KEY] = user.email
            preferences[PHOTO_URL_KEY] = user.photoUrl
        }
    }


    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}