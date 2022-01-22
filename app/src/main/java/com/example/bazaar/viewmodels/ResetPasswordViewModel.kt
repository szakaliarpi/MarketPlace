package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.ResetPasswordRequest
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class ResetPasswordViewModel(val context: Context, val repository: Repository) : ViewModel() {
    var user = SingleLiveEvent<User>()
    var error: MutableLiveData<String> = MutableLiveData()
    var success: SingleLiveEvent<Boolean> = SingleLiveEvent()

    init {
        user.value = User()
    }

    suspend fun resetPassword() {
        val request =
                ResetPasswordRequest(username = user.value!!.username, email = user.value!!.email)
        try {
            val result = repository.resetPassword(request)
            success.value = true


        } catch (e: Exception) {
            error.value = e.message.toString()
            Log.d("ResetPasswordViewModel", "ResetPasswordViewModel - exception: $e")
        }
    }
}