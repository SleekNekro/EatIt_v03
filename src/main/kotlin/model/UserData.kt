package com.github.SleekNekro.model

import kotlinx.serialization.Serializable

enum class Role {
    ADMIN, USER, MODERATOR
}

@Serializable
data class UserData(
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: Role = Role.USER,
)
