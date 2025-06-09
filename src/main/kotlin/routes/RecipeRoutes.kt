package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.RecipeDAO
import com.github.SleekNekro.model.request.RecipeRequest
import com.github.SleekNekro.model.request.UpdateRecipeRequest
import com.github.SleekNekro.util.SseBroadcaster
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val recipeBroadcaster = SseBroadcaster()

@OptIn(DelicateCoroutinesApi::class)
fun startSseHeartbeat() {
    println("🚀 `startSseHeartbeat()` se ha ejecutado!")  // 🔥 Log de inicio
    GlobalScope.launch {
        while (true) {
            println("🔍 Intentando enviar evento SSE")
            recipeBroadcaster.broadcast("keep_alive", "{\"message\": \"Ping SSE\"}")
            delay(5000)
        }
    }
}

fun Route.configureRecipeRoutes() {
    println("🔍 Clientes SSE conectados: ${recipeBroadcaster.clients.size}")
    // Endpoint SSE para actualizaciones en tiempo real
    get("/events") {
        call.response.headers.append(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
        recipeBroadcaster.addClient(call)
        println("🔍 Cliente SSE conectado!")
        println("🔍 Lista de clientes actualizada: ${recipeBroadcaster.clients.size}")  // 🔥 Confirmar almacenamiento
    }


    get {
        val recipes = RecipeDAO.getAllRecipes().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, recipes)
    }

    get("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() 
            ?: return@get call.respondInvalidId()
        val recipe = RecipeDAO.getRecipeById(id)?.toDataClass()
            ?: return@get call.respondNotFound("Receta")
        call.respond(HttpStatusCode.OK, recipe)
    }

    get("/all/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val recipes = RecipeDAO.getRecipesByUser(userId).map { it.toDataClass() }
        if (recipes.isEmpty()) {
            call.respond(HttpStatusCode.NotFound, "El usuario no tiene recetas")
        } else {
            call.respond(HttpStatusCode.OK, recipes)
        }
    }

    get("/recipes/search") {
        try {
            val query = call.parameters["query"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "El parámetro de búsqueda es requerido")
            )

            val recetas = RecipeDAO.findByName(query)
            call.respond(HttpStatusCode.OK, recetas)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Error al buscar recetas: ${e.message}")
            )
        }
    }

    post {
        val recipeRequest = call.receive<RecipeRequest>()
        val newRecipe = RecipeDAO.createRecipe(
            userId = recipeRequest.userId,
            title = recipeRequest.title,
            description = recipeRequest.description,
            servings = recipeRequest.servings,
            imageUrl = recipeRequest.imageUrl
        )
        
        // Notificar a los clientes sobre la nueva receta
        recipeBroadcaster.broadcast(
            "recipe_update",
            Json.encodeToString(
                mapOf(
                    "type" to "create",
                    "recipe" to newRecipe.toDataClass()
                )
            )

        )
        call.respond(HttpStatusCode.Created, newRecipe.toDataClass())
    }

    patch("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@patch call.respondInvalidId()
        val updateRequest = call.receive<UpdateRecipeRequest>()
        
        val updated = RecipeDAO.updateRecipe(
            id = id,
            newTitle = updateRequest.title,
            newDescription = updateRequest.description,
            newServings = updateRequest.servings,
            newImageUrl = updateRequest.imageUrl
        )

        if (updated) {
            // Obtener la receta actualizada y notificar a los clientes
            val updatedRecipe = RecipeDAO.getRecipeById(id)?.toDataClass()
            updatedRecipe?.let {
                recipeBroadcaster.broadcast(
                    "recipe_update",
                    Json.encodeToString(
                        mapOf(
                            "type" to "update",
                            "recipe" to it
                        )
                    )
                )
            }
            call.respond(HttpStatusCode.OK, "Receta actualizada correctamente")
        } else {
            call.respondNotFound("Receta")
        }
    }

    delete("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@delete call.respondInvalidId()

        if (RecipeDAO.deleteRecipe(id)) {
            // Notificar a los clientes sobre la eliminación
            recipeBroadcaster.broadcast(
                "recipe_update",
                Json.encodeToString(
                    mapOf(
                        "type" to "delete",
                        "recipeId" to id
                    )
                )
            )
            call.respond(HttpStatusCode.OK, "Receta eliminada correctamente")
        } else {
            call.respondNotFound("Receta")
        }
    }
}