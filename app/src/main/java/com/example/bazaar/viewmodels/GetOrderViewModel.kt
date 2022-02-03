package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.GetOrdersListResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class GetOrderViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var getOrderListResponse = SingleLiveEvent<GetOrdersListResponse>()

    val filter: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val sort: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    suspend fun getOrder() {

        val token = MyApplication.sharedPreferences.getStringValue(
                SharedPreferencesManager.KEY_TOKEN,
                "Empty token!"
        )

        try {
            getOrderListResponse.value = repository.getOrders(token.toString(), filter.value.toString(), sort.value.toString())
            removeSpecialCharacters()
            Log.d("GetOrderViewModel", getOrderListResponse.value.toString())

        } catch (e: Exception) {

            Log.d("GetOrderViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

    private fun removeSpecialCharacters() {

        for (i in getOrderListResponse.value!!.orders.indices) {
            getOrderListResponse.value!!.orders[i].title = getOrderListResponse.value!!.orders[i].title.replace("\"", "")
            getOrderListResponse.value!!.orders[i].description = getOrderListResponse.value!!.orders[i].description.replace("\"", "")
            getOrderListResponse.value!!.orders[i].order_id = getOrderListResponse.value!!.orders[i].order_id.replace("\"", "")
            getOrderListResponse.value!!.orders[i].owner_username = getOrderListResponse.value!!.orders[i].owner_username.replace("\"", "")
            getOrderListResponse.value!!.orders[i].status = getOrderListResponse.value!!.orders[i].status.replace("\"", "")
            getOrderListResponse.value!!.orders[i].username = getOrderListResponse.value!!.orders[i].username.replace("\"", "")
            getOrderListResponse.value!!.orders[i].price_per_unit = getOrderListResponse.value!!.orders[i].price_per_unit.replace("\"", "")
            getOrderListResponse.value!!.orders[i].units = getOrderListResponse.value!!.orders[i].units.replace("\"", "")
        }
    }

}