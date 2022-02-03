package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.AddProductRequest
import com.example.bazaar.api.model.AddProductResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import retrofit2.HttpException

class AddProductViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var product = SingleLiveEvent<AddProductRequest>()
    var response = SingleLiveEvent<AddProductResponse>()

    init {
        product.value = AddProductRequest("", "", 0, "", false, 0f, "", "")
    }

    suspend fun addProduct() {
        val request =
                AddProductRequest(
                        title = product.value!!.title,
                        description = product.value!!.description,
                        price_per_unit = product.value!!.price_per_unit,
                        units = product.value!!.units,
                        is_active = product.value!!.is_active,
                        rating = product.value!!.rating,
                        amount_type = product.value!!.amount_type,
                        price_type = product.value!!.price_type)
        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            response.value = repository.addProduct(token!!, request)

        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    if (e.code() == 302) {
                        Log.d("AddProductViewModel", "Token expired: $e")
                    } else {
                        Log.d("AddProductViewModel", "AddProductViewModel - exception: $e")
                        error.value = e.message.toString()
                    }
                }
                else -> {
                    Log.d("AddProductViewModel", "exception: $e")
                    error.value = e.message.toString()
                }
            }

        }
    }

}