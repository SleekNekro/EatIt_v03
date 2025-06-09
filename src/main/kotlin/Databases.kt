package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

// mysql://root:vtmQNCsHGrZQExcofMOHJNHZiJtvipbq@caboose.proxy.rlwy.net:49981/railway
fun Application.configureDatabases() {
    val database = Database.connect(
        url = System.getenv("DATABASE_URL"),
        driver = System.getenv("DATABASE_DRIVER"),
        user = System.getenv("DATABASE_USER"),
        password = System.getenv("DATABASE_PASSWORD")
    )
}