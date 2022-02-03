package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.AddOrderRequest
import com.example.bazaar.api.model.AddOrderResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class AddOrderViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var order = SingleLiveEvent<AddOrderRequest>()
    var response = SingleLiveEvent<AddOrderResponse>()

    init {
        order.value = AddOrderRequest("", "", 0, 0, "", "")
    }

    suspend fun addOrder() {
        val request =
                AddOrderRequest(
                        title = order.value!!.title,
                        description = order.value!!.description,
                        price_per_unit = order.value!!.price_per_unit,
                        units = order.value!!.units,
                        owner_username = order.value!!.owner_username,
                        revolut_link = "")
        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            response.value = repository.addOrder(token!!, request)

        } catch (e: Exception) {
            Log.d("AddOrderViewModel", "exception: $e")
            error.value = e.message.toString()
        }

    }
}

