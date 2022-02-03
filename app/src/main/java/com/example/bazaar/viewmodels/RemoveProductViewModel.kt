package com.example.bazaar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.MyApplication
import com.example.bazaar.SingleLiveEvent
import com.example.bazaar.api.model.RemoveProductResponse
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository

class RemoveProductViewModel(val context: Context, private val repository: Repository) : ViewModel() {
    var error: MutableLiveData<String> = MutableLiveData()
    var removeProductResponse = SingleLiveEvent<RemoveProductResponse>()

    suspend fun removeProduct(product_id: String) {
        try {

            val token = MyApplication.sharedPreferences.getStringValue(
                    SharedPreferencesManager.KEY_TOKEN,
                    "Empty token!"
            )

            removeProductResponse.value = repository.removeProduct(token!!, product_id)

        } catch (e: Exception) {

            Log.d("RemoveProductViewModel", "exception: $e")
            error.value = e.message.toString()

        }
    }

}