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

fun Route.uploadRoute(){
    post("/{filename}"){
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)
        var fileDescription = ""

        //val username = extractPrincipalUsername(call)!! //TODO
        val username = "tempUser"
        val filename = call.parameters["filename"]!!

        //ensure this exists
        createDirectories(uploadDir/username)
        val file: java.io.File = (uploadDir / username / filename).toFile()

        environment.log.info("Receiving file from $username")

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val file = (uploadDir / username / filename).toFile()
                    part.provider().copyAndClose(file.writeChannel())

                    call.respond(HttpStatusCode.Created)
                }
                else -> {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            part.dispose()
        }
    }
}