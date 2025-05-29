package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.CommentDAO
import com.github.SleekNekro.model.request.CommentRequest
import com.github.SleekNekro.model.request.UpdateCommentRequest
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureCommentRoutes() {
    get {
        val comments = CommentDAO.getAllComments().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, comments)
    }

    get("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()
        val comment = CommentDAO.getCommentById(id)?.toDataClass()
            ?: return@get call.respondNotFound("Comentario")
        call.respond(HttpStatusCode.OK, comment)
    }

    get("/recipe/{recipeId}") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val comments = CommentDAO.getCommentsByRecipeId(recipeId).map { it.toDataClass() }
        if (comments.isEmpty()) {
            call.respond(HttpStatusCode.NotFound, "No hay comentarios para esta receta")
        } else {
            call.respond(HttpStatusCode.OK, comments)
        }
    }

    post {
        val commentRequest = call.receive<CommentRequest>()
        val newComment = CommentDAO.createComment(
            recipeId = commentRequest.recipeId,
            userId = commentRequest.userId,
            content = commentRequest.content
        )
        call.respond(HttpStatusCode.Created, newComment.toDataClass())
    }

    patch("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@patch call.respondInvalidId()
        val updateRequest = call.receive<UpdateCommentRequest>()

        if (CommentDAO.updateComment(id, updateRequest.content)) {
            call.respond(HttpStatusCode.OK, "Comentario actualizado correctamente")
        } else {
            call.respondNotFound("Comentario")
        }
    }

    delete("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@delete call.respondInvalidId()

        if (CommentDAO.deleteComment(id)) {
            call.respond(HttpStatusCode.OK, "Comentario eliminado correctamente")
        } else {
            call.respondNotFound("Comentario")
        }
    }
}