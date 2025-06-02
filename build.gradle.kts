import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project
val commons_codec_version: String by project



plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
}


tasks {
    shadowJar {
        // Define el nombre del jar que se generará
        archiveFileName.set("EatIt_v03.jar")
        mergeServiceFiles() // Opcional, útil si necesitas fusionar archivos de servicios
    }

    // Tarea 'stage' requerida por Heroku
    register("stage") {
        dependsOn("shadowJar")
    }
}

group = "com.github.SleekNekro"
version = "0.0.1"

application {
    mainClass.set("com.github.SleekNekro.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=false")
}

repositories {
    mavenCentral()
}

//ktor {
//    docker {
//        // Nombre de la imagen generada localmente y su tag
//        localImageName.set("EatIt_v03")
//        imageTag.set("latest")
//
//        // Opcional: especifica el puerto expuesto si lo deseas (por defecto, la app usa el puerto indicado en la variable PORT)
//        // exposedPort.set(8085)
//
//        // Configuración para subir la imagen a Docker Hub (si es que quieres publicarla)
//        externalRegistry.set(
//            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
//                // Puedes cambiar el appName para que coincida con tu imagen
//                appName = provider { "EatIt_v03" },
//                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
//                password = providers.environmentVariable("DOCKER_HUB_PASSWORD"),
//            )
//        )
//    }
//}



// Configuración para las tareas Kotlin
tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}



dependencies {
    // Core de Ktor
    implementation("io.ktor:ktor-server-core") // Puede que quieras consolidar esta línea con la siguiente
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")

    // Content Negotiation
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // Base de datos
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.jetbrains.exposed:exposed-java-time:0.42.0")

    // Seguridad
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.1.2")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("commons-codec:commons-codec:$commons_codec_version")
    implementation("org.mindrot:jbcrypt:0.4")

    // Logging y Thymeleaf
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-thymeleaf:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:3.1.2")

    // Otras dependencias
    implementation("io.ktor:ktor-server-call-logging:2.0.0")
    implementation("io.ktor:ktor-server-core:${ktor_version}")
    implementation("io.ktor:ktor-server-cors:${ktor_version}")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io:2.15.1")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}