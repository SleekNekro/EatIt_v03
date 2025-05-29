package com.github.SleekNekro.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.apache.commons.fileupload.servlet.ServletFileUpload
import java.io.File
import java.util.UUID

fun Route.configureImageRoutes() {
    val uploadDir = File("uploads").apply { mkdirs() }

    post("/upload") {
        val multipart = ServletFileUpload()
        multipart.setSizeMax(5 * 1024 * 1024) // 5MB límite

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

                    call.respond(hashMapOf("location" to "/uploads/$fileName"))
                }
                else -> {}
            }
            part.dispose()
        }
    }

    static("/uploads") {
        files(uploadDir)
    }
}
