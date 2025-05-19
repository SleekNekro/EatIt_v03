package com.github.SleekNekro

import com.github.SleekNekro.data.DAO.CommentDAO
import com.github.SleekNekro.data.DAO.RecipeDAO
import com.github.SleekNekro.data.DAO.UserDAO
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

                // Validación del email
                if (!registerRequest.email.contains("@")) {
                    call.respond(HttpStatusCode.BadRequest, "Formato de correo inválido")
                    return@put
                }

                // Validación de la contraseña
                if (registerRequest.password.length < 6) {
                    call.respond(HttpStatusCode.BadRequest, "La contraseña debe tener al menos 6 caracteres")
                    return@put
                }

                // Verificar si el correo ya está registrado
                if (UserDAO.getUserByEmail(registerRequest.email) != null) {
                    call.respond(HttpStatusCode.Conflict, "${registerRequest.email} ya está en uso")
                    return@put
                }

                // Encriptar la contraseña antes de guardarla
                val hashedPassword = hashPassword(registerRequest.password)

                // Crear un nuevo usuario
                val user = UserDAO.createUser(
                    username = registerRequest.username,
                    email = registerRequest.email,
                    password = hashedPassword,
                    profilePic = null
                )

                call.respond(HttpStatusCode.Created, mapOf("message" to "${user.username} registrado correctamente"))
            }

            post("/login") {
                val loginRequest = call.receive<LoginRequest>()

                // Buscar usuario por email en lugar de username
                val user = UserDAO.getUserByEmail(loginRequest.email)?.toDataClass()

                if (user == null || !verifyPassword(loginRequest.password, user.password)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Email o contraseña inválidos"))
                    return@post
                }

                // Generar el token JWT
                val token = generateToken(user, jwtConfig.secret, jwtConfig.domain, jwtConfig.audience)

                call.respond(
                    HttpStatusCode.OK, mapOf(
                        "token" to token,
                        "user" to mapOf(
                            "id" to user.id,
                            "username" to user.username,
                            "email" to user.email,
                            "profilePic" to user.profilePic,
                            "followers" to user.followers
                        )
                    )
                )
            }


            authenticate("auth-jwt") {
                route("/user") {

                    // Obtener todos los usuarios
                    get {
                        val users = UserDAO.getAllUsers().map { it.toDataClass() }
                        call.respond(HttpStatusCode.OK, users)
                    }

                    // Obtener un usuario por ID
                    get("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@get
                        }

                        val user = UserDAO.getUserById(id)?.toDataClass()
                        if (user == null) {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        } else {
                            call.respond(HttpStatusCode.OK, user)
                        }
                    }

                    // Obtener un usuario por email
                    get("/email/{email}") {
                        val email = call.parameters["email"]
                        if (email.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid email format")
                            return@get
                        }

                        val user = UserDAO.getUserByEmail(email)?.toDataClass()
                        if (user == null) {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        } else {
                            call.respond(HttpStatusCode.OK, user)
                        }
                    }

                    // Registrar un nuevo usuario
                    post("/register") {
                        val registerRequest = call.receive<RegisterRequest>()

                        if (registerRequest.username.isBlank() || registerRequest.email.isBlank() || registerRequest.password.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, "Missing required fields")
                            return@post
                        }

                        if (UserDAO.getUserByEmail(registerRequest.email) != null) {
                            call.respond(HttpStatusCode.Conflict, "Email already used")
                            return@post
                        }

                        val hashedPassword = hashPassword(registerRequest.password)

                        val user = UserDAO.createUser(
                            username = registerRequest.username,
                            email = registerRequest.email,
                            password = hashedPassword,

                            profilePic = null
                        )

                        call.respond(
                            HttpStatusCode.Created,
                            mapOf("message" to "${user.username} registered successfully")
                        )
                    }

                    // Actualizar un usuario
                    patch("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@patch
                        }

                        val param = call.receiveParameters()
                        val updated = UserDAO.updateUser(
                            id = id,
                            newUsername = param["username"],
                            newEmail = param["email"],
                            newPassword = param["password"],
                            newProfilePic = param["profilePic"]
                        )

                        if (updated) {
                            call.respond(HttpStatusCode.OK, "User updated successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        }
                    }

                    // Eliminar un usuario
                    delete("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@delete
                        }

                        val deleted = UserDAO.deleteUser(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "User deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        }
                    }
                }

                route("/recipe") {

                    // Obtener todas las recetas
                    get {
                        val recipes = RecipeDAO.getAllRecipes().map { it.toDataClass() }
                        call.respond(HttpStatusCode.OK, recipes)
                    }

                    // Obtener una receta por ID
                    get("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@get
                        }

                        val recipe = RecipeDAO.getRecipeById(id)?.toDataClass()
                        if (recipe == null) {
                            call.respond(HttpStatusCode.NotFound, "Recipe not found")
                        } else {
                            call.respond(HttpStatusCode.OK, recipe)
                        }
                    }

                    // Obtener todas las recetas de un usuario
                    get("/all/{userId}") {
                        val userId = call.parameters["userId"]?.toLongOrNull()
                        if (userId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
                            return@get
                        }

                        val recipes = RecipeDAO.getRecipesByUser(userId).map { it.toDataClass() }
                        if (recipes.isEmpty()) {
                            call.respond(HttpStatusCode.NotFound, "User has no recipes")
                        } else {
                            call.respond(HttpStatusCode.OK, recipes)
                        }
                    }

                    // Crear una nueva receta
                    post {
                        val params = call.receiveParameters()
                        val userId = params["userId"]?.toLongOrNull()
                        val title = params["title"]
                        val description = params["description"]
                        val servings = params["servings"]?.toIntOrNull()
                        val imageUrl = params["imageUrl"]

                        if (userId == null || title.isNullOrBlank() || description.isNullOrBlank() || servings == null) {
                            call.respond(HttpStatusCode.BadRequest, "Missing or invalid parameters")
                            return@post
                        }

                        val newRecipe = RecipeDAO.createRecipe(userId, title, description, servings, imageUrl)
                        call.respond(HttpStatusCode.Created, newRecipe.toDataClass())
                    }

                    // Actualizar una receta
                    patch("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@patch
                        }

                        val params = call.receiveParameters()
                        val updated = RecipeDAO.updateRecipe(
                            id = id,
                            newTitle = params["title"],
                            newDescription = params["description"],
                            newServings = params["servings"]?.toIntOrNull(),
                            newImageUrl = params["imageUrl"]
                        )

                        if (updated) {
                            call.respond(HttpStatusCode.OK, "Recipe updated successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Recipe not found")
                        }
                    }

                    // Eliminar una receta
                    delete("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@delete
                        }

                        val deleted = RecipeDAO.deleteRecipe(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "Recipe deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Recipe not found")
                        }
                    }
                }

                route("/comment") {

                    // Obtener todos los comentarios
                    get {
                        val comments = CommentDAO.getAllComments().map { it.toDataClass() }
                        call.respond(HttpStatusCode.OK, comments)
                    }

                    // Obtener un comentario por ID
                    get("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@get
                        }

                        val comment = CommentDAO.getCommentById(id)?.toDataClass()
                        if (comment == null) {
                            call.respond(HttpStatusCode.NotFound, "Comment not found")
                        } else {
                            call.respond(HttpStatusCode.OK, comment)
                        }
                    }

                    // Obtener todos los comentarios de una receta específica
                    get("/recipe/{recipeId}") {
                        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
                        if (recipeId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid recipe ID format")
                            return@get
                        }

                        val comments = CommentDAO.getCommentsByRecipeId(recipeId).map { it.toDataClass() }
                        if (comments.isEmpty()) {
                            call.respond(HttpStatusCode.NotFound, "No comments found for this recipe")
                        } else {
                            call.respond(HttpStatusCode.OK, comments)
                        }
                    }

                    // Crear un nuevo comentario
                    post {
                        val params = call.receiveParameters()
                        val recipeId = params["recipeId"]?.toLongOrNull()
                        val userId = params["userId"]?.toLongOrNull()
                        val content = params["content"]

                        if (recipeId == null || userId == null || content.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, "Missing or invalid parameters")
                            return@post
                        }

                        val newComment = CommentDAO.createComment(recipeId, userId, content)
                        call.respond(HttpStatusCode.Created, newComment.toDataClass())
                    }

                    // Actualizar un comentario
                    patch("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@patch
                        }

                        val params = call.receiveParameters()
                        val newContent = params["content"]

                        if (newContent.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, "Missing or invalid content")
                            return@patch
                        }

                        val updated = CommentDAO.updateComment(id, newContent)
                        if (updated) {
                            call.respond(HttpStatusCode.OK, "Comment updated successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Comment not found")
                        }
                    }

                    // Eliminar un comentario
                    delete("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                            return@delete
                        }

                        val deleted = CommentDAO.deleteComment(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "Comment deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Comment not found")
                        }
                    }
                }

                route("/like"){}
            }
        }
    }
}
