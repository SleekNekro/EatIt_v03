package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val id: Long,
    val username: String,
    val email: String,
    val password: String,
    val profilePic: String?,
    val followers: Int,
    val createdAt: Long
)
