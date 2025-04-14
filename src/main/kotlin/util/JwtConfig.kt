package com.github.SleekNekro.util

import io.ktor.server.application.*

data class JwtConfig(
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String
)

fun Application.getJwtConfig(): JwtConfig {
    val config = environment.config
    return JwtConfig(
        domain = config.property("jwt.domain").getString(),
        audience = config.property("jwt.audience").getString(),
        realm = config.property("jwt.realm").getString(),
        secret = config.property("jwt.secret").getString()
    )
}