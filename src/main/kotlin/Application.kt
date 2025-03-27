package com.github.SleekNekro

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json



fun main(args: Array<String>) {
    embeddedServer(Netty, port = 9292, host = "127.0.0.1") {
        module()
    }.start()
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }


    configureSerialization()
    configureDatabases()
    configureRouting()
}
