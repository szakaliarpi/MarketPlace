package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.UpdateProductRequest
import com.example.bazaar.api.model.UpdateProductRespone
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class UpdateProductViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var updateProductResponse = SingleLiveEvent<UpdateProductRespone>()
    var updateProductRequest = SingleLiveEvent<UpdateProductRequest>()

    init {
        updateProductRequest.value = UpdateProductRequest(0, false, "", 0f, "", "")
    }

    suspend fun updateProduct(product_id: String) {

        val request =
                UpdateProductRequest(
                        price_per_unit = updateProductRequest.value!!.price_per_unit,
                        is_active = updateProductRequest.value!!.is_active,
                        title = updateProductRequest.value!!.title,
                        rating = updateProductRequest.value!!.rating,
                        amount_type = updateProductRequest.value!!.amount_type,
                        price_type = updateProductRequest.value!!.price_type,
                )

        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            updateProductResponse.value = repository.updateProduct(token!!, product_id, request)

        } catch (e: Exception) {

            Log.d("UpdateProductViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}