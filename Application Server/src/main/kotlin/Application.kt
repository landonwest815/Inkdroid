package com.all

import com.application_server.plugins.configureSecurity
import com.application_server.plugins.configureSerialization
import com.application_server.repository.NotesRepository
import com.application_server.repository.UserRepository
import com.application_server.routing.configureRouting
import com.application_server.service.JwtService
import com.application_server.service.UserService

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val userRepository = UserRepository()
    val userService = UserService(userRepository)
    val jwtService = JwtService(this, userService)

    val notesRepository = NotesRepository()

    configureSerialization()
    configureSecurity(jwtService)
    configureRouting(jwtService, userService, notesRepository)
}
