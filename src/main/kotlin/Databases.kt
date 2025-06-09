package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

// mysql://root:vtmQNCsHGrZQExcofMOHJNHZiJtvipbq@caboose.proxy.rlwy.net:49981/railway
fun Application.configureDatabases() {
    // Obtenemos la URL original de la variable de entorno
    val originalUrl = System.getenv("MYSQL_URL")
        ?: error("MYSQL_URL no está definida")

    // Convertimos el protocolo a JDBC
    val jdbcUrl = originalUrl.replaceFirst("mysql://", "jdbc:mysql://") +
            "?serverTimezone=UTC&useSSL=false"

    // Puedes seguir utilizando las variables separadas si lo prefieres:
    // val dbUser = System.getenv("MYSQLUSER") ?: error("MYSQLUSER no está definida")
    // val dbPassword = System.getenv("MYSQLPASSWORD") ?: error("MYSQLPASSWORD no está definida")

    // Si las credenciales ya vienen embebidas en la URL, no es necesario pasarlas de forma separada,
    // pero en algunos casos es preferible separarlas. Esto depende de cómo maneje la conexión el driver.

    val database = Database.connect(
        url = jdbcUrl,
        driver = "com.mysql.cj.jdbc.Driver"
        // Si decides pasar usuario y contraseña separados, asegúrate de que no estén duplicados en la URL.
        // user = dbUser,
        // password = dbPassword
    )
    println("Database connected: $database")
}

