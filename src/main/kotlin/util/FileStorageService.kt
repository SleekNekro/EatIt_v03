package com.github.SleekNekro.util

import java.io.File

object FileStorageService {
    private const val UPLOAD_DIR = "upload/"

    fun saveFile(filename: String, fileBytes: ByteArray): String {
        val file = File(UPLOAD_DIR, filename)
        file.writeBytes(fileBytes)

        // Devuelve la ruta relativa donde se guardó el archivo
        return "/$UPLOAD_DIR$filename"
    }
}