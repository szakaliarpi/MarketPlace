package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.LoginRequest
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import retrofit2.HttpException

class LoginViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var token: SingleLiveEvent<String> = SingleLiveEvent()
    var error: MutableLiveData<String> = MutableLiveData()
    var user = SingleLiveEvent<User>()

    init {
        user.value = User()
    }

    suspend fun login() {
        val request =
                LoginRequest(username = user.value!!.username, password = user.value!!.password)
        try {
            val result = repository.login(request)

            // sets token
            MyApplication.sharedPreferences.putStringValue(SharedPreferencesManager.KEY_TOKEN, result.token)
            token.value = result.token

            // sets phone number and email for user
            user.value!!.phone_number = result.phone_number.toString()
            user.value!!.email = result.email.toString()

            MyApplication.sharedPreferences.putUserValue(SharedPreferencesManager.KEY_USER, user.value!!)

            Log.d("LoginViewModel", "token = " + MyApplication.sharedPreferences.getStringValue(SharedPreferencesManager.KEY_TOKEN, "Empty token!"))
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    if (e.code() == 302) {
                        Log.d("LoginViewModel", "Token expired: $e")
                        error.value = e.message.toString()
                    } else {
                        Log.d("LoginViewModel", "LoginViewModel - exception: $e")
                        error.value = e.message.toString()
                    }
                }
                else -> {
                    Log.d("LoginViewModel", "LoginViewModel - exception: $e")
                    error.value = e.message.toString()
                }
            }

        }
    }

}