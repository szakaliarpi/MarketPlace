package com.example.bazaar

import android.app.Application
import com.example.bazaar.manager.SharedPreferencesManager

class MyApplication : Application() {
    companion object {
        lateinit var sharedPreferences: SharedPreferencesManager
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = SharedPreferencesManager(applicationContext)
    }
}