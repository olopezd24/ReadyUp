package com.example.readyup.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class MeResponse(
    val id: Int,
    val username: String,
    val email: String
)