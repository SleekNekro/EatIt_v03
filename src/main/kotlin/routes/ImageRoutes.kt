package com.github.SleekNekro.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.log
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import org.apache.commons.fileupload.servlet.ServletFileUpload
import java.io.File
import java.util.UUID

@Suppress("DEPRECATION")
fun Route.configureImageRoutes() {
    val uploadDir = File("uploads").apply { mkdirs() }

    post("/upload") {
        try {
            val multipart = ServletFileUpload()
            multipart.setSizeMax(5 * 1024 * 1024)

            val request = call.receive<MultiPartData>()
            request.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val ext = File(part.originalFileName ?: "").extension
                        val fileName = "${UUID.randomUUID()}.$ext"
                        val file = File(uploadDir, fileName)
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                        println("Imagen guardada correctamente en: ${file.absolutePath}")
                        val files = uploadDir.listFiles()
                        files?.forEach { println("Archivo guardado: ${it.name}") }
                        val imageUrl = "https://eatitv03-production.up.railway.app/uploads/$fileName"
                        call.respond(hashMapOf("location" to imageUrl))
                    }
                    else -> {}
                }
                part.dispose()
            }
        } catch (e: Exception) {
            // Loguea el error para información adicional
            println("Error al cargar la imagen: ${e.message}")
            // Si el canal ya se cerró, podrías simplemente finalizar sin lanzar más excepciones
            call.respondText("Error interno", status = HttpStatusCode.InternalServerError)
        }
    }


    static("/uploads") {
        files(uploadDir)
    }
}
