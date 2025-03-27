package com.github.SleekNekro.model

import com.github.SleekNekro.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CommentData(
    val id : Int,
    val content : String,
    val userId : Int,
    val postId : Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt : LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt : LocalDateTime
)
