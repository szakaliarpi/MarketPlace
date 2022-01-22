package com.example.bazaar.api.model

import com.squareup.moshi.JsonClass

data class User(var username: String = "", var password: String = "", var email: String = "", var phone_number: String = "")

@JsonClass(generateAdapter = true)
data class LoginRequest(
        var username: String,
        var password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
        var username: String,
        var email: String,
        var phone_number: Int,
        var token: String,
        var creation_time: Long,
        var refresh_time: Long
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
        var username: String,
        var password: String,
        var email: String,
        var phone_number: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserDataRequest(
        var username: String,
        var phone_number: Long
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
        var code: String,
        var message: String,
        var creation_time: Long
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
        var username: String,
        var email: String,
)

@JsonClass(generateAdapter = true)
data class ResetPasswordResponse(
        var code: Int,
        var message: String,
        var timestamp: Long
)

@JsonClass(generateAdapter = true)
data class GetUserInfoResponse(
        var username: String,
        var phone_number: Long,
        var email: String,
        var is_activated: Boolean,
        var creation_time: Long
)

@JsonClass(generateAdapter = true)
data class GetUserInfoListResponse(
        val code: Long,
        val data: List<GetUserInfoResponse>,
        val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class UpdateUserDataResponse(
        val username: String,
        val phone_number: Long,
        val email: String,
        val is_activated: Boolean,
        val creation_time: Long,
        val token: String,
)

@JsonClass(generateAdapter = true)
data class UpdateUserDataListResponse(
        val code: Int,
        val updatedData: UpdateUserDataResponse,
        val timestamp: Long
)