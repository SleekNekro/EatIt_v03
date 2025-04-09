val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
}

group = "com.github.SleekNekro"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}
ktor{
    docker{
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "ktor-server" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD"),
            )
        )
    }
}

dependencies {
    // Core de Ktor: Librerías esenciales para ejecutar el servidor y manejar la configuración básica
    implementation("io.ktor:ktor-server-core") // Núcleo del servidor Ktor
    implementation("io.ktor:ktor-server-netty") // Motor de servidor Netty para ejecutar Ktor
    implementation("io.ktor:ktor-server-config-yaml") // Permite configurar Ktor usando archivos YAML

    // Content Negotiation: Gestión de formatos de respuesta como JSON
    implementation("io.ktor:ktor-server-content-negotiation") // Negociación de contenido para la API
    implementation("io.ktor:ktor-serialization-kotlinx-json") // Serialización JSON con kotlinx.serialization

    // Base de datos: Librerías para conectarse y trabajar con bases de datos
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version") // Exposed: núcleo del ORM
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version") // Exposed: soporte para JDBC
    implementation("com.h2database:h2:$h2_version") // Driver de base de datos H2 para pruebas locales
    implementation("mysql:mysql-connector-java:8.0.33") // Driver MySQL para conectar con bases de datos MySQL
    implementation("org.jetbrains.exposed:exposed-java-time:0.42.0")


    // Seguridad: Plugins de autenticación y autorización para proteger endpoints
    implementation("io.ktor:ktor-server-auth:$ktor_version") // Autenticación para Ktor

    // Logging: Librerías para el registro de eventos y depuración
    implementation("ch.qos.logback:logback-classic:$logback_version") // Logback para gestión robusta de logs
    //implementation("io.ktor:ktor-server-call-logging:$ktor_version")


    // Testing: Dependencias para escribir y ejecutar pruebas de tu aplicación
    testImplementation("io.ktor:ktor-server-test-host") // Herramientas de prueba para endpoints de Ktor
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version") // Pruebas de Kotlin con soporte para JUnit
}
