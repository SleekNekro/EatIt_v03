package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.CommentDAO
import com.github.SleekNekro.data.Comments
import com.github.SleekNekro.util.*
import com.github.SleekNekro.util.SseBroadcaster
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val commentBroadcaster = SseBroadcaster()

fun Route.configureCommentRoutes() {

    get("/comments/events") {
        commentBroadcaster.addClient(call)
    }

    get {
        val comments = CommentDAO.getAllComments().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, comments)
    }

    get("/{recipeId}") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()
        val comments = CommentDAO.getRecipeComments(recipeId).map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, comments)
    }

//    post {
//        val commentRequest = runCatching { call.receiveNullable<Comments>() }.getOrNull()
//            ?: return@post call.respondMissingBody()
//        val result = CommentDAO.createComment(commentRequest)
//        result?.let {
//            commentBroadcaster.broadcast("new_comment", Json.encodeToString(it.toDataClass()))
//            call.respond(HttpStatusCode.Created, it.toDataClass())
//        } ?: call.respondInternalError("No se pudo crear el comentario")
//    }

    delete("/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@delete call.respondInvalidId()
        val deleted = CommentDAO.deleteComment(id)
        if (deleted) {
            call.respond(HttpStatusCode.OK, "Comentario eliminado correctamente")
        } else {
            call.respondNotFound("Comentario")
        }
    }
}
