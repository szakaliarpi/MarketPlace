package com.example.bazaar.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.bazaar.api.model.User
import com.google.gson.Gson

class SharedPreferencesManager(context: Context) {

    private val SHARED_PREFERENCES_NAME = "MarketPlaceSharedPreferences"
    private var preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "SHARED_PREFERENCES_KEY_TOKEN"
        const val KEY_USER = "SHARED_PREFERENCES_KEY_USER"
    }

    fun putStringValue(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun getStringValue(key: String, defaultValue: String): String? {
        return preferences.getString(key, defaultValue)
    }

    fun putUserValue(key: String, value: User) {
        val jsonUser: String = Gson().toJson(value)
        putStringValue(key, jsonUser)
    }

    fun getUserValue(key: String, defaultValue: User): User {
        val jsonUserDefault: String = Gson().toJson(defaultValue)
        val jsonUser: String? = getStringValue(key, jsonUserDefault)
        return Gson().fromJson(jsonUser, User::class.java)
    }

}