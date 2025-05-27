package com.github.SleekNekro.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val profilePic: String?,
    val followers: Int
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse
)
