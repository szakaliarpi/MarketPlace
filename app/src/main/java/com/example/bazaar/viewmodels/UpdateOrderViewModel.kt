package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.UpdateOrderRequest
import com.example.bazaar.api.model.UpdateOrderResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class UpdateOrderViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var updateOrderResponse = SingleLiveEvent<UpdateOrderResponse>()
    var updateOrderRequest = SingleLiveEvent<UpdateOrderRequest>()

    init {
        updateOrderRequest.value = UpdateOrderRequest(0, "", "")
    }

    suspend fun updateOrder(order_id: String) {

        val request =
                UpdateOrderRequest(
                        price_per_unit = updateOrderRequest.value!!.price_per_unit,
                        status = updateOrderRequest.value!!.status,
                        title = updateOrderRequest.value!!.title
                )

        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            updateOrderResponse.value = repository.updateOrder(token!!, order_id, request)

            Log.d("UpdateOrderViewModel", updateOrderResponse.value.toString())


        } catch (e: Exception) {

            Log.d("UpdateOrderViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}