package com.github.SleekNekro.security

import io.ktor.server.application.*

data class JwtConfig(
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String
)

fun Application.getJwtConfig(): JwtConfig {
    return JwtConfig(
        domain = System.getenv("JWT_DOMAIN") ?: "https://jwt-provider-domain/",
        audience = System.getenv("JWT_AUDIENCE") ?: "jwt-audience",
        realm = System.getenv("JWT_REALM") ?: "ktor sample app",
        secret = System.getenv("JWT_SECRET") ?: "secret"
    )
}