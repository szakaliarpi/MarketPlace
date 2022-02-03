package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class RefreshTokenViewModel(val context: Context, val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var success: SingleLiveEvent<Boolean> = SingleLiveEvent()

    suspend fun refreshToken() {
        val token = MyApplication.sharedPreferences.getStringValue(
                SharedPreferencesManager.KEY_TOKEN,
                "Empty token!"
        )
        try {
            val result = repository.refreshToken(token.toString())
            success.value = true
            MyApplication.sharedPreferences.putStringValue(SharedPreferencesManager.KEY_TOKEN, result.token)

            Log.d("RefreshTokenViewModel", result.toString())
        } catch (e: Exception) {
            error.value = e.message.toString()
            Log.d("RefreshTokenViewModel", e.toString())

        }


    }
}