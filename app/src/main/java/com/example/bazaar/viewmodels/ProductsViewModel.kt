package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.ProductsListResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import retrofit2.HttpException

class ProductsViewModel(val context: Context, val repository: Repository) : ViewModel() {
    val products: SingleLiveEvent<ProductsListResponse> = SingleLiveEvent()
    var error: MutableLiveData<String> = MutableLiveData()
    var success: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val filter: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val sort: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    suspend fun getProducts() {

        val token = MyApplication.sharedPreferences.getStringValue(
                SharedPreferencesManager.KEY_TOKEN,
                "Empty token!"
        )
        try {

            products.value = repository.getProducts(token.toString(), filter.value.toString(), sort.value.toString())
            removeSpecialCharacters()
            success.value = true

        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    if (e.code() == 302) {
                        error.value = "302"
                        Log.d("ProductsViewModel", "Token expired: $e")
                    }
                    if (e.code() == 301) {
                        error.value = "301"
                        Log.d("ProductsViewModel", "Invalid token: $e")
                    } else {
                        Log.d("ProductsViewModel", "AddProductViewModel - exception: $e")
                        error.value = e.message.toString()
                    }
                }
                else -> {
                    error.value = e.message.toString()
                    Log.d("ProductsViewModel", "exception: $e")
                }
            }
        }

    }

    private fun removeSpecialCharacters() {
        for (i in products.value!!.products.indices) {
            products.value!!.products[i].title = products.value!!.products[i].title.replace("\"", "")
            products.value!!.products[i].price_type = products.value!!.products[i].price_type.replace("\"", "")
            products.value!!.products[i].amount_type = products.value!!.products[i].amount_type.replace("\"", "")
            products.value!!.products[i].description = products.value!!.products[i].description.replace("\"", "")
            products.value!!.products[i].price_per_unit = products.value!!.products[i].price_per_unit.replace("\"", "")
            products.value!!.products[i].units = products.value!!.products[i].units.replace("\"", "")
        }
    }
}
