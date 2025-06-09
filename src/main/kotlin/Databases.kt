package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

// mysql://root:vtmQNCsHGrZQExcofMOHJNHZiJtvipbq@caboose.proxy.rlwy.net:49981/railway
fun Application.configureDatabases() {
    // Opción 1: Usar MYSQL_URL directamente (si Railway lo define)
    val databaseUrl = System.getenv("MYSQL_URL")
        ?: "jdbc:mysql://${System.getenv("MYSQLHOST")}:${System.getenv("MYSQLPORT")}/${System.getenv("MYSQLDATABASE")}?serverTimezone=UTC&useSSL=false"
    val dbUser = System.getenv("MYSQLUSER") ?: error("Falta MYSQLUSER")
    val dbPassword = System.getenv("MYSQLPASSWORD") ?: error("Falta MYSQLPASSWORD")

    // Usamos el driver de MySQL
    val database = Database.connect(
        url = databaseUrl,
        driver = "com.mysql.cj.jdbc.Driver",
        user = dbUser,
        password = dbPassword
    )
    println("Database connected: $database")
}
