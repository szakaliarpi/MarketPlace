package com.example.bazaar.viewmodels

import androidx.lifecycle.ViewModel
import com.example.bazaar.api.model.ProductsListResponse

class MainActivityViewModel : ViewModel() {
    var products: ProductsListResponse? = null
}