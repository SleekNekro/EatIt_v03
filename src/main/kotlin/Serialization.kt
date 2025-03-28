package com.github.SleekNekro

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual


fun Application.configureSerialization() {
    val json = Json {
        serializersModule = SerializersModule {
            contextual(LocalDateTimeSerializer)
        }
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter)) // Serializa LocalDateTime como cadena ISO-8601
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter) // Deserializa ISO-8601 a LocalDateTime
    }
}