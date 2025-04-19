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
import java.nio.file.Files.createDirectories

val uploadDir = Path("uploads/")

fun Route.uploadRoute(){
    get("/{filename}"){
        val username = extractPrincipalUsername(call)!!
        val filename = call.parameters["filename"]!!
        call.respondFile((uploadDir / username / filename).toFile())
    }

    post("/{filename}"){
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)
        var fileDescription = ""

        val username = extractPrincipalUsername(call)!!
        val filename = call.parameters["filename"]!!

        //ensure this exists
        createDirectories(uploadDir/username)
        val file: java.io.File = (uploadDir / username / filename).toFile()

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    fileDescription = part.value
                }

                is PartData.FileItem -> {
                    part.provider().copyAndClose(file.writeChannel())
                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(HttpStatusCode.Created)

    }
}