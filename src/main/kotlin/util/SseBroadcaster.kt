package com.github.SleekNekro.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList

class SseBroadcaster {
    val clients = CopyOnWriteArrayList<ApplicationCall>()
    private val mutex = Mutex()

    suspend fun addClient(call: ApplicationCall) {
        mutex.withLock {
            clients.add(call)
        }
        println("🔍 Cliente añadido, total clientes: ${clients.size}")  // 🔥 Confirmación antes de iniciar la transmisión

        call.response.cacheControl(CacheControl.NoCache(null))
        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            try {
                while (true) {
                    try {
                        write("data: {\"message\": \"Ping SSE\"}\n\n")
                        flush()
                        kotlinx.coroutines.delay(15_000)
                    } catch (e: Exception) {
                        println("⚠️ Error en SSE: ${e.message}")
                        break // 🔥 Detener el loop en caso de fallo
                    }
                }
            } catch (e: ClosedWriteChannelException) {
                println("⚠️ Cliente SSE desconectado: ${call.request.origin.remoteHost}")
                println("🔍 Total clientes: ${clients.size}")  // 🔥 Confirmación antes de iniciar la transmisión

            } finally {
                mutex.withLock {
                    println("⚠️ Eliminando cliente SSE: ${call.request.origin.remoteHost}")
                    clients.remove(call)
                    println("🔍 Total clientes: ${clients.size}")  // 🔥 Confirmación antes de iniciar la transmisión
                }
            }
        }
    }


    suspend fun broadcast(event: String, data: String) {
        //println("🔍 Enviando evento SSE: event=$event, data=$data")  // 🔥 Log en el servidor
        //println("🔍 Clientes SSE conectados: ${clients.size}")  // 🔥 Verificar clientes antes de enviar eventos
        mutex.withLock {
            clients.forEach { call ->
                try {
                    call.respondTextWriter(ContentType.Text.EventStream) {
                        write("event: $event\n")
                        write("data: $data\n\n")  // 🔥 Formato correcto con doble salto de línea
                        flush()  // 🔥 Forzar envío inmediato de datos
                    }
                } catch (e: Exception) {
                    println("⚠️ Error enviando SSE: ${e.message}")
                }
            }
        }
    }
}