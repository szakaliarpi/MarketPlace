package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.RegisterRequest
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class RegisterViewModel(val context: Context, val repository: Repository) : ViewModel() {
    var user = SingleLiveEvent<User>()
    var error: MutableLiveData<String> = MutableLiveData()
    var success: MutableLiveData<Boolean> = MutableLiveData()

    init {
        user.value = User()
    }

    suspend fun register() {
        val request =
                RegisterRequest(username = user.value!!.username, password = user.value!!.password, email = user.value!!.email, phone_number = user.value!!.phone_number)
        try {
            val result = repository.register(request)
            success.value = true


        } catch (e: Exception) {
            error.value = e.message.toString()
            Log.d("RegisterViewModel", "RegisterViewModel - exception: $e")
        }
    }
}