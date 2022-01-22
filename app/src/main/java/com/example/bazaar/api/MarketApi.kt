package com.example.bazaar.api

import com.example.bazaar.api.model.*
import com.example.bazaar.utils.Constants
import retrofit2.http.*

interface MarketApi {
    @POST(Constants.LOGIN_URL)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(Constants.REGISTER_URL)
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST(Constants.RESETPASSWORD_URL)
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    @GET(Constants.GET_PRODUCT_URL)
    suspend fun getProducts(@Header(Constants.HEADER_TOKEN) token: String,
                            @Header(Constants.HEADER_FILTER) filter: String,
                            @Header(Constants.HEADER_SORT) sort: String,
                            @Header(Constants.HEADER_LIMIT) limit: Int): ProductsListResponse

    @GET(Constants.REFRESH_TOKEN_URL)
    suspend fun refreshToken(@Header(Constants.HEADER_TOKEN) token: String): RefreshTokenResponse

    @Multipart
    @POST(Constants.ADD_PRODUCT_URL)
    suspend fun addProduct(@Header(Constants.HEADER_TOKEN) token: String,
                           @Part("title") title: String,
                           @Part("description") description: String,
                           @Part("price_per_unit") price_per_unit: String,
                           @Part("units") units: String,
                           @Part("is_active") is_active: Boolean,
                           @Part("rating") rating: Double,
                           @Part("amount_type") amount_type: String,
                           @Part("price_type") price_type: String): AddProductResponse

    @GET(Constants.GET_USER_INFO_URL)
    suspend fun getUserInfo(@Header(Constants.HEADER_USERNAME) username: String): GetUserInfoListResponse

    @Multipart
    @POST(Constants.UPDATE_USER_DATA_URL)
    suspend fun updateUserData(
            @Header(Constants.HEADER_TOKEN) token: String,
            @Part("username") username: String,
            @Part("phone_number") phone_number: Long,
    ): UpdateUserDataListResponse

    @POST(Constants.REMOVE_PRODUCT_URL)
    suspend fun removeProduct(@Header(Constants.HEADER_TOKEN) token: String, @Query(Constants.QUERY_PRODUCT_ID) product_id: String): RemoveProductResponse

    @POST(Constants.UPDATE_PRODUCT_URL)
    suspend fun updateProduct(@Header(Constants.HEADER_TOKEN) token: String,
                              @Query(Constants.QUERY_PRODUCT_ID) product_id: String,
                              @Body request: UpdateProductRequest): UpdateProductRespone

    @GET(Constants.GET_ORDERS_URL)
    suspend fun getOrders(@Header(Constants.HEADER_TOKEN) token: String,
                          @Header(Constants.HEADER_FILTER) filter: String,
                          @Header(Constants.HEADER_SORT) sort: String,
                          @Header(Constants.HEADER_LIMIT) limit: Int): GetOrdersListResponse

    @Multipart
    @POST(Constants.ADD_ORDER_URL)
    suspend fun addOrder(@Header(Constants.HEADER_TOKEN) token: String,
                         @Part("title") title: String,
                         @Part("description") description: String,
                         @Part("price_per_unit") price_per_unit: Int,
                         @Part("units") units: Int,
                         @Part("owner_username") owner_username: String): AddOrderResponse

    @POST(Constants.REMOVE_ORDER_URL)
    suspend fun removeOrder(@Header(Constants.HEADER_TOKEN) token: String, @Query(Constants.QUERY_ORDER_ID) order_id: String): RemoveOrderResponse

    @POST(Constants.UPDATE_ORDER_URL)
    suspend fun updateOrder(@Header(Constants.HEADER_TOKEN) token: String,
                            @Query(Constants.QUERY_ORDER_ID) order_id: String,
                            @Body request: UpdateOrderRequest): UpdateOrderResponse
}