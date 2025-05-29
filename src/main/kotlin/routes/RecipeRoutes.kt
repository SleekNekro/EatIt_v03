package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.RecipeDAO
import com.github.SleekNekro.model.request.RecipeRequest
import com.github.SleekNekro.model.request.UpdateRecipeRequest
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureRecipeRoutes() {
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

    post {
        val recipeRequest = call.receive<RecipeRequest>()
        val newRecipe = RecipeDAO.createRecipe(
            userId = recipeRequest.userId,
            title = recipeRequest.title,
            description = recipeRequest.description,
            servings = recipeRequest.servings,
            imageUrl = recipeRequest.imageUrl
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
            call.respond(HttpStatusCode.OK, "Receta actualizada correctamente")
        } else {
            call.respondNotFound("Receta")
        }
    }

    delete("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@delete call.respondInvalidId()

        if (RecipeDAO.deleteRecipe(id)) {
            call.respond(HttpStatusCode.OK, "Receta eliminada correctamente")
        } else {
            call.respondNotFound("Receta")
        }
    }
}