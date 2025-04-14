package com.github.SleekNekro

import com.github.SleekNekro.dao.*
import com.github.SleekNekro.dao.UserDAO.Companion.getUserByEmail
import com.github.SleekNekro.dao.UserDAO.Companion.getUserByUsername
import com.github.SleekNekro.model.UserData
import com.github.SleekNekro.model.request.LoginRequest
import com.github.SleekNekro.model.request.RegisterRequest
import com.github.SleekNekro.util.generateToken
import com.github.SleekNekro.util.getJwtConfig
import com.github.SleekNekro.util.hashPassword
import com.github.SleekNekro.util.verifyPassword
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val jwtConfig = getJwtConfig()
    routing {
        staticResources("/", "static")

        route("/auth") {
            put("/register") {
                val registerRequest = call.receive<RegisterRequest>()

                if (getUserByEmail(registerRequest.email) != null) {
                    call.respond(HttpStatusCode.Conflict, "${registerRequest.email} already used")
                    return@put
                }

                val hashedPassword = hashPassword(registerRequest.password)

                val user = UserData(
                    name = registerRequest.username,
                    email = registerRequest.email,
                    password = hashedPassword
                )

                UserDAO.registerUser(user)
                call.respond(HttpStatusCode.Created, "${registerRequest.username} registered successfully")
            }
            post("/login") {
                val loginRequest = call.receive<LoginRequest>()

                val user = getUserByUsername(loginRequest.username)?.toDataClass()

                if (user == null || !verifyPassword(loginRequest.password, user.password)) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Email o contraseña inválidos")
                    )
                    return@post
                }

                val token = generateToken(user, jwtConfig.secret, jwtConfig.domain, jwtConfig.audience)

                call.respond(HttpStatusCode.OK, mapOf("token" to token))
            }

        }
        authenticate("auth-jwt") {
            route("/user") {
                // Get all users
                get {
                    val users = UserDAO.getAllUsers() // Directly call the companion object method
                    call.respond(users.map { it.toDataClass() }) // Convert DAO to data class
                }

                // Get a single user by ID
                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }

                    val user = UserDAO.getUserById(id)?.toDataClass() // Properly call toDataClass()
                    if (user == null) {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    } else {
                        call.respond(user) // Respond directly with the data class
                    }
                }

                //Get a user by eMail
                get("/{email}") {
                    val email = call.parameters["email"]?.toCharArray()
                    if (email == null) {
                        call.respondText("Invalid Email", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    val user = UserDAO.getUserByEmail(email.contentToString())?.toDataClass()
                    if (user == null) {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    } else call.respond(user)
                }

                // Create a new user
                post {
                    val name = call.receiveParameters()["name"].toString()
                    val email = call.receiveParameters()["email"].toString()
                    val password = call.receiveParameters()["password"].toString()
                    if (name.isNullOrBlank()) {
                        call.respondText("Missing 'name'", status = HttpStatusCode.BadRequest)
                        return@post
                    }

                    val newUser = UserDAO.createUser(name, email, password) // Directly call the companion object method
                    call.respond(newUser.toDataClass()) // Convert DAO to data class
                }

                // Update a user
                patch("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@patch
                    }


                    val param = call.receiveParameters()
                    val name = param["name"]
                    val email = param["email"]
                    val password = param["password"]

                    val updated = UserDAO.updateUser(id = id, newName = name, newEmail = email, newPassw = password)
                    if (updated) {
                        call.respondText("User updated successfully")
                    } else {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    }
                }

                // Delete a user
                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@delete
                    }

                    val deleted = UserDAO.deleteUser(id) // Directly call the companion object method
                    if (deleted) {
                        call.respondText("User deleted successfully")
                    } else {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    }
                }
            }
            route("/post") {
                get {
                    val post = PostDAO.getAllPosts()
                    call.respond(post.map { it.toDataClass() })
                }

                put {
                    val title = call.receiveParameters()["title"].toString()
                    val content = call.receiveParameters()["content"].toString()
                    val imageUrl = call.receiveParameters()["imageUrl"].toString()

                    if (title.isNullOrBlank()) {
                        call.respondText("Missing 'title'", status = HttpStatusCode.BadRequest)
                        return@put
                    }

                    val newPost = PostDAO.createPost(title, content, imageUrl)
                    call.respond(newPost.toDataClass())
                }

                patch("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@patch
                    }

                    val param = call.receiveParameters()
                    val title = param["title"]
                    val content = param["content"].toString()
                    val imageUrl = param["imageUrl"].toString()

                    val updatedPost =
                        PostDAO.updatePost(id = id, newTitle = title, newContent = content, newImageUrl = imageUrl)
                    call.respond(updatedPost)

                    if (updatedPost) {
                        call.respondText("User updated successfully")
                    } else call.respondText("User not found", status = HttpStatusCode.NotFound)

                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }

                    val post = PostDAO.getPostById(id)?.toDataClass() // Properly call toDataClass()
                    if (post == null) {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    } else {
                        call.respond(post) // Respond directly with the data class
                    }
                }
                get("/all/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    val post = PostDAO.getAllPostsOfUser(id).map { it.toDataClass() }
                    if (post.isEmpty()) {
                        call.respondText("User no Posts", status = HttpStatusCode.NotFound)
                    }
                    call.respond(post)
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@delete
                    }
                    val delete = PostDAO.deletePostById(id) ?: return@delete
                    if (delete) {
                        call.respondText("Post deleted successfully")
                    } else call.respondText("User not found", status = HttpStatusCode.NotFound)

                }
            }
            route("/comment") {
                get {
                    val com = CommentDAO.getAllComments()
                    call.respond(com.map { it.toDataClass() })
                }
                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                    }
                    val content = call.receiveParameters()["content"].toString()
                    val com = CommentDAO.createComment(content)
                    call.respond(com)
                }
                patch("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@patch
                    }
                    val content = call.receiveParameters()["content"].toString()
                    val com = CommentDAO.updateComment(id, newContent = content)
                    call.respond(com)

                    if (com) {
                        call.respondText("Comment updated successfully")
                    } else call.respondText("User not found", status = HttpStatusCode.NotFound)
                }
                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    val comment = CommentDAO.getCommentById(id)?.toDataClass() ?: return@get
                    call.respond(comment)
                }
                get("/all/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    val com = CommentDAO.getCommentsByPostID(id).map { it.toDataClass() }
                    if (com.isEmpty()) {
                        call.respondText("Comment not found", status = HttpStatusCode.NotFound)
                    }
                    call.respond(com)
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                        return@delete
                    }

                    val delete = CommentDAO.deleteComment(id) ?: return@delete
                    if (delete) {
                        call.respondText("Comment deleted succesfully")
                    } else call.respondText("Comment not found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}
