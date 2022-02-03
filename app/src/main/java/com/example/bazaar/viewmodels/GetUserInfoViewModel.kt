package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.GetUserInfoListResponse
import com.example.bazaar.repository.Repository

class GetUserInfoViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var userResponse = SingleLiveEvent<GetUserInfoListResponse>()

    suspend fun getUserInfo(username: String) {

        try {
            userResponse.value = repository.getUserInfo(username)

        } catch (e: Exception) {

            Log.d("GetUserInfoViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}