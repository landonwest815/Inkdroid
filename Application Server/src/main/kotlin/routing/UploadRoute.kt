package com.application_server.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.file
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import kotlin.io.path.Path
import kotlin.io.path.div
import io.ktor.utils.io.*
import java.nio.file.Files.createDirectories

val uploadDir = Path("uploads/")

fun Route.uploadRoute() {
    post("/{uploader}/{filename}") {
        val uploader = call.parameters["uploader"]!!      // ← now captures the first segment
        val filename = call.parameters["filename"]!!      // ← second segment

        // ensure directory exists
        createDirectories(uploadDir / uploader)

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100).forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val dest = (uploadDir / uploader / filename).toFile()
                    environment.log.info("Receiving $filename from $uploader")
                    part.provider().copyAndClose(dest.writeChannel())
                    call.respond(HttpStatusCode.Created)
                }
                else -> call.respond(HttpStatusCode.BadRequest)
            }
            part.dispose()
        }
    }
}