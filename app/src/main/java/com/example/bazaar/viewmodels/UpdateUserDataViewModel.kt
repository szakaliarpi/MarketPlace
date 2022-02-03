package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.UpdateUserDataListResponse
import com.example.bazaar.api.model.UpdateUserDataRequest
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class UpdateUserDataViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var updateUserDataListResponse = SingleLiveEvent<UpdateUserDataListResponse>()
    var updateUserDataRequest = SingleLiveEvent<UpdateUserDataRequest>()

    init {
        updateUserDataRequest.value = UpdateUserDataRequest("", 0)
    }

    suspend fun updateUserData() {

        val request =
                UpdateUserDataRequest(
                        username = updateUserDataRequest.value!!.username,
                        phone_number = updateUserDataRequest.value!!.phone_number)

        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            updateUserDataListResponse.value = repository.updateUserData(token!!, request)

            Log.d("UpdateUserDataViewModel", updateUserDataListResponse.value.toString())

            MyApplication.sharedPreferences.putStringValue(SharedPreferencesManager.KEY_TOKEN, updateUserDataListResponse.value!!.updatedData.token)
            MyApplication.sharedPreferences.getUserValue(SharedPreferencesManager.KEY_USER, User()).username = updateUserDataListResponse.value!!.updatedData.username

        } catch (e: Exception) {

            Log.d("UpdateUserDataViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}