package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/testDB",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "1234"
    )
}