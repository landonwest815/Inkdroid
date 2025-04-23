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

fun Route.downloadRoute() {
    // 1) List _all_ files, prefixed by uploader
    get("/file_names") {
        val names = mutableListOf<String>()
        val base = uploadDir.toFile()
        if (base.exists() && base.isDirectory) {
            base.listFiles { it.isDirectory }?.forEach { userDir ->
                userDir.listFiles { f -> f.isFile }?.forEach { f ->
                    // we use slash here so client can split on “/”
                    names.add("${userDir.name}/${f.name}")
                }
            }
        }
        call.respond(names)
    }

    // 2) Serve any user’s file
    get("/{uploader}/{filename}") {
        val uploader = call.parameters["uploader"]!!
        val filename = call.parameters["filename"]!!
        val file = (uploadDir / uploader / filename).toFile()

        if (!file.exists()) {
            environment.log.info("Download of $filename from $uploader Failed")
            call.respond(HttpStatusCode.NotFound, "File not found")
            return@get
        }
        if (!file.canRead()) {
            environment.log.info("Download of $filename from $uploader Failed")
            call.respond(HttpStatusCode.InternalServerError, "Cannot read file")
            return@get
        }
        environment.log.info("Download of $filename Succeeded")
        call.respondFile(file)
    }

    // 3) DELETE any user’s file
    delete("/{uploader}/{filename}") {
        val uploader = call.parameters["uploader"]!!
        val filename = call.parameters["filename"]!!
        val file = (uploadDir / uploader / filename).toFile()

        when {
            !file.exists() ->
                call.respond(HttpStatusCode.NotFound,"Not found")
            file.delete() -> {
                // remove empty folder if you like
                val dir = file.parentFile
                if (dir.listFiles()?.isEmpty() == true) dir.delete()
                call.respond(HttpStatusCode.OK)
            }
            else ->
                call.respond(HttpStatusCode.InternalServerError,"Could not delete")
        }
    }
}
