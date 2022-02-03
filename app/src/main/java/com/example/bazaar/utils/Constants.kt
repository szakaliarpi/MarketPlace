package com.example.bazaar.utils

object Constants {
    const val BASE_URL = "https://pure-gorge-51703.herokuapp.com/"
    const val LOGIN_URL = "user/login"
    const val REGISTER_URL = "user/register"
    const val RESETPASSWORD_URL = "user/reset"
    const val GET_PRODUCT_URL = "products"
    const val REFRESH_TOKEN_URL = "user/refresh"
    const val ADD_PRODUCT_URL = "products/add"
    const val UPDATE_PRODUCT_URL = "products/update"
    const val GET_USER_INFO_URL = "user/data"
    const val UPDATE_USER_DATA_URL = "user/update"
    const val REMOVE_PRODUCT_URL = "products/remove"
    const val GET_ORDERS_URL = "orders"
    const val ADD_ORDER_URL = "orders/add"
    const val REMOVE_ORDER_URL = "orders/remove"
    const val UPDATE_ORDER_URL = "orders/update"

    /**
     * Headers.
     */
    const val HEADER_TOKEN = "token"
    const val HEADER_SORT = "sort"
    const val HEADER_LIMIT = "limit"
    const val HEADER_FILTER = "filter"
    const val HEADER_SKIP = "skip"
    const val HEADER_USERNAME = "username"

    /**
     * Queries
     */

    const val QUERY_PRODUCT_ID = "product_id"
    const val QUERY_ORDER_ID = "order_id"
}