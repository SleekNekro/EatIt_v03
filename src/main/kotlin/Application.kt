package com.github.SleekNekro

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.SleekNekro.security.getJwtConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.MultiPartData
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver


fun main(args: Array<String>) {
    // Heroku define la variable de entorno PORT; en caso de que no esté definida, usa el puerto 8085 (ideal para pruebas locales)
    val port = System.getenv("PORT")?.toInt() ?: 8085
    // Para que Heroku acepte las conexiones, debemos escuchar en "0.0.0.0"
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}


fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(CallLogging) {
        level = Level.DEBUG
    }
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "static/"
            suffix = ".html"
            characterEncoding = "UTF-8"
        })
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }



    val jwtConfig = getJwtConfig()
    authentication {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.domain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience))
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

    configureSerialization()
    configureDatabases()
    configureRouting()
}