package com.github.SleekNekro.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.SleekNekro.data.UserData
import java.util.*

fun generateToken(
    user: UserData,
    secret: String,
    issuer: String,
    audience: String
): String? {
    val expiresAt = Date(System.currentTimeMillis() + 60 * 60 * 1000)
    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("id", user.id)
        .withClaim("email", user.email)
        .withClaim("role", user.role.name)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(secret))
}