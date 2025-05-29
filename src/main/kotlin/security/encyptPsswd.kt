package com.github.SleekNekro.security

import org.mindrot.jbcrypt.BCrypt

fun hashPassword(password: String): String {
    // Si la contraseña ya parece un hash de BCrypt, retornarlo tal cual
    if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
        println("⚠️ La contraseña ya está hasheada, retornando sin modificar")
        return password
    }

    println("\n🔐 DEBUG REGISTRO:")
    println("📝 Password a hashear: '$password'")
    
    val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
    println("🎯 Hash generado: $hashed")
    return hashed
}

fun verifyPassword(password: String, hashedPassword: String): Boolean {
    println("\n🔐 DEBUG LOGIN:")
    println("📝 Password recibida: '$password'")
    println("🔑 Hash almacenado: $hashedPassword")
    
    return BCrypt.checkpw(password, hashedPassword)
}