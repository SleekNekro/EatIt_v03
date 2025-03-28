package com.github.SleekNekro.model

import com.github.SleekNekro.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

@Serializable
data class PostData(
    val id: Int,
    val title: String,
    val content: String,
    val userId: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
    val imageUrl: String?
)
