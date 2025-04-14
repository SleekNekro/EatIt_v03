package com.github.SleekNekro

import com.github.SleekNekro.util.LocalDateTimeSerializer
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

