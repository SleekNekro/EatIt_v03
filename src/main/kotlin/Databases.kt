package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

// Ejemplo de la variable inyectada por Railway:
// mysql://root:vtmQNCsHGrZQExcofMOHJNHZiJtvipbq@caboose.proxy.rlwy.net:49981/
// Se espera que, si no se especifica la base de datos, se añada manualmente (por ejemplo, "eatit")
fun Application.configureDatabases() {
    // Obtenemos la URL original desde la variable de entorno
    val originalUrl = System.getenv("MYSQL_URL")
        ?: error("MYSQL_URL no está definida")

    // Si la URL termina en "/" significa que no se indicó el nombre de la BD.
    // En ese caso, añadimos la base de datos por defecto ("eatit").
    val finalUrl = if (originalUrl.endsWith("/")) {
        originalUrl + "eatit"
    } else {
        originalUrl
    }

    // Convertimos el protocolo a JDBC y configuramos parámetros para TLS/SSL
    // Dado que el servidor activa TLS con un certificado autofirmado, usamos:
    // useSSL=true, verifyServerCertificate=false, allowPublicKeyRetrieval=true
    val jdbcUrl = finalUrl.replaceFirst("mysql://", "jdbc:mysql://") +
            "?serverTimezone=UTC&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true"

    println("Constructed JDBC URL: $jdbcUrl")

    val database = Database.connect(
        url = jdbcUrl,
        driver = "com.mysql.cj.jdbc.Driver"
    )
    println("Database connected: $database")
}
