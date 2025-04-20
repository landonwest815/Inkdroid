package com.application_server.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import kotlin.io.path.Path
import kotlin.io.path.div
import io.ktor.utils.io.*
import java.io.File
import java.nio.file.Files.createDirectories

fun Route.downloadRoute(){
    get("/file_names"){
        //val username = extractPrincipalUsername(call)!! //TODO
        val username = "tempUser"

        val userDir = (uploadDir / username).toFile()

        val fileNames = mutableListOf<String>()
        environment.log.info("$username is downloading file names")

        if (userDir.exists() && userDir.isDirectory) {
            val files = userDir.listFiles()
            if (files != null) {
                for (file in files) {
                    fileNames.add(file.name)
                }
            }
        }

        call.respond(fileNames)
    }

    get("/{filename}"){
        //val username = extractPrincipalUsername(call)!! //TODO
        val username = "tempUser"
        val filename = call.parameters["filename"]!!

        val file = (uploadDir / username / filename).toFile()
        environment.log.info("$username is downloading file data: $filename")

        if (!file.canRead()) {
            call.respond(HttpStatusCode.InternalServerError, "File cannot be read")
            return@get
        }

        if (!file.exists()) {
            call.respond(HttpStatusCode.NotFound, "File not found")
            return@get
        }

        call.respondFile(file)
    }
}