package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.model.request.*
import com.github.SleekNekro.security.JwtConfig
import com.github.SleekNekro.security.generateToken
import com.github.SleekNekro.security.hashPassword
import com.github.SleekNekro.security.verifyPassword
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureAuthRoutes(jwtConfig: JwtConfig) {
    post("/register") {
        val registerRequest = call.receive<RegisterRequest>()
        
        if (!registerRequest.email.contains("@")) {
            call.respondInvalidFormat("email")
            return@post
        }

        if (registerRequest.password.length < 6) {
            call.respond(HttpStatusCode.BadRequest, "La contraseña debe tener al menos 6 caracteres")
            return@post
        }

        if (UserDAO.getUserByEmail(registerRequest.email) != null) {
            call.respond(HttpStatusCode.Conflict, "${registerRequest.email} ya está en uso")
            return@post
        }

        val hashedPassword = hashPassword(registerRequest.password)
        val user = UserDAO.createUser(
            username = registerRequest.username,
            email = registerRequest.email,
            password = hashedPassword,
            profilePic = null
        )

        call.respond(HttpStatusCode.Created, mapOf("message" to "${user.username} registrado correctamente"))
    }

    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            val userEntity = UserDAO.getUserByEmail(loginRequest.email)
                ?: return@post call.respondInvalidCredentials()

            val user = userEntity.toDataClass()
            val passwordMatch = verifyPassword(loginRequest.password, user.password)

            if (passwordMatch) {
                val token = generateToken(user, jwtConfig.secret, jwtConfig.domain, jwtConfig.audience)
                token?.let {
                    call.respond(HttpStatusCode.OK, LoginResponse(
                        token = it,
                        user = UserResponse.fromUser(user)
                    ))
                }
            } else {
                call.respondInvalidCredentials()
            }
        } catch (e: Exception) {
            call.respondServerError(e)
        }
    }
}