package com.github.SleekNekro

import com.github.SleekNekro.routes.*
import com.github.SleekNekro.security.getJwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    println("🚀 Configuración de rutas cargada!")
    val jwtConfig = getJwtConfig()
    
    routing {

        staticResources("/", "static")
        
        route("/auth") {
            configureAuthRoutes(jwtConfig)
        }
        
        //authenticate("auth-jwt") {
            route("/user") { configureUserRoutes() }
            route("/recipe") { configureRecipeRoutes() }
            route("/comment") { configureCommentRoutes() }
            route("/like") { configureLikeRoutes() }
            route("/ingredient") { configureIngredientRoutes() }
            configureImageRoutes()
            configureFollowerRoutes()

        //}
        environment.log.info("Rutas registradas en el servidor:")
        application.environment.log.info(application.attributes.toString())
    }
}