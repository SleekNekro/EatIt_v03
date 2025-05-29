package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.IngredientDAO
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureIngredientRoutes() {
    get {
        val ingredients = IngredientDAO.getAllIngredients().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, ingredients)
    }

    get("/recipe/{recipeId}") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val ingredients = IngredientDAO.getIngredientsByRecipe(recipeId).map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, ingredients)
    }

    post {
        val params = call.receiveParameters()
        val ingredientData = params.extractIngredientData()
            ?: return@post call.respondInvalidParameters()

        val ingredient = IngredientDAO.addIngredient(
            recipeId = ingredientData.recipeId,
            name = ingredientData.name,
            quantity = ingredientData.quantity
        )
        call.respond(HttpStatusCode.Created, ingredient.toDataClass())
    }

    delete("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@delete call.respondInvalidId()

        if (IngredientDAO.removeIngredient(id)) {
            call.respond(HttpStatusCode.OK, "Ingrediente eliminado correctamente")
        } else {
            call.respondNotFound("Ingrediente")
        }
    }
}