package com.github.SleekNekro.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondInvalidId() {
    respond(HttpStatusCode.BadRequest, "ID inválido")
}

suspend fun ApplicationCall.respondNotFound(resource: String) {
    respond(HttpStatusCode.NotFound, "$resource no encontrado")
}

suspend fun ApplicationCall.respondInvalidParameters() {
    respond(HttpStatusCode.BadRequest, "Parámetros inválidos o faltantes")
}

suspend fun ApplicationCall.respondInvalidFormat(field: String) {
    respond(HttpStatusCode.BadRequest, "Formato de $field inválido")
}

suspend fun ApplicationCall.respondInvalidCredentials() {
    respond(HttpStatusCode.Unauthorized, mapOf("error" to "Email o contraseña inválidos"))
}

suspend fun ApplicationCall.respondServerError(e: Exception) {
    println("⚠️ Error: ${e.message}")
    e.printStackTrace()
    respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
}