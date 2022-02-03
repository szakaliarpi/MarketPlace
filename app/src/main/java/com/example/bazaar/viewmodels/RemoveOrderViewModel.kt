package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.RemoveOrderResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class RemoveOrderViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var removeOrderResponse = MutableLiveData<RemoveOrderResponse>()

    suspend fun removeOrder(order_id: String) {
        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            removeOrderResponse.value = repository.removeOrder(token!!, order_id)

        } catch (e: Exception) {

            Log.d("RemoveOrderViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}