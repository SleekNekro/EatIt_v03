package com.github.SleekNekro

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

// mysql://root:vtmQNCsHGrZQExcofMOHJNHZiJtvipbq@caboose.proxy.rlwy.net:49981/railway
fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:mysql://caboose.proxy.rlwy.net:49981/eatit",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "vtmQNCsHGrZQExcofMOHJNHZiJtvipbq"
    )
}