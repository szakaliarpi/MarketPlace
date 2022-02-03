package com.example.bazaar.repository

import com.example.bazaar.api.RetrofitInstance
import com.example.bazaar.api.model.*

class Repository {
    suspend fun login(request: LoginRequest): LoginResponse {
        return RetrofitInstance.api.login(request)
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return RetrofitInstance.api.register(request)
    }

    suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        return RetrofitInstance.api.resetPassword(request)
    }

    suspend fun getProducts(token: String, filter: String = ""//"{\"username\":\"Sutyii\"}"
                            , sort: String = ""//"{\"title\" : 1 }"
                            , limit: Int = 1000): ProductsListResponse {
        return RetrofitInstance.api.getProducts(token, filter, sort, limit)
    }

    suspend fun refreshToken(token: String): RefreshTokenResponse {
        return RetrofitInstance.api.refreshToken(token)
    }

    suspend fun addProduct(token: String, request: AddProductRequest): AddProductResponse {
        return RetrofitInstance.api.addProduct(
                token,
                request.title,
                request.description,
                request.price_per_unit.toString(),
                request.units, request.is_active,
                request.rating.toDouble(),
                request.amount_type,
                request.price_type)
    }

    suspend fun getUserInfo(username: String): GetUserInfoListResponse {
        return RetrofitInstance.api.getUserInfo(username)
    }

    suspend fun updateUserData(token: String, request: UpdateUserDataRequest): UpdateUserDataListResponse {
        return RetrofitInstance.api.updateUserData(token, request.username, request.phone_number)
    }

    suspend fun removeProduct(token: String, product_id: String): RemoveProductResponse {
        return RetrofitInstance.api.removeProduct(token, product_id)
    }

    suspend fun updateProduct(token: String, product_id: String, request: UpdateProductRequest): UpdateProductRespone {
        return RetrofitInstance.api.updateProduct(token, product_id, request)
    }

    suspend fun getOrders(
            token: String,
            filter: String = "",//"{\"username\":\"Sutyii\"}"
            sort: String = "",//"{\"title\" : 1 }"
            limit: Int = 1000,
    ): GetOrdersListResponse {
        return RetrofitInstance.api.getOrders(token, filter, sort, limit)
    }

    suspend fun addOrder(token: String, request: AddOrderRequest): AddOrderResponse {
        return RetrofitInstance.api.addOrder(
                token,
                request.title,
                request.description,
                request.price_per_unit,
                request.units,
                request.owner_username)
    }

    suspend fun removeOrder(token: String, order_id: String): RemoveOrderResponse {
        return RetrofitInstance.api.removeOrder(token, order_id)
    }

    suspend fun updateOrder(token: String, order_id: String, request: UpdateOrderRequest): UpdateOrderResponse {
        return RetrofitInstance.api.updateOrder(token, order_id, request)
    }
}