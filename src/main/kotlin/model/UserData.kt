package com.github.SleekNekro.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val id : Int,
    val name : String,
    val email : String,
    val password : String,
)
