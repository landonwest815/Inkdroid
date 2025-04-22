package com.application_server.routing

import com.application_server.model.Note
import com.application_server.model.User
import com.application_server.repository.NotesRepository
import com.ktor.routing.request.UserRequest
import com.application_server.routing.response.UserResponse
import com.application_server.service.UserService
import com.application_server.util.hashPassword
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.helloRoute(){
  get(){
      call.respondText("Hello World!")
  }
}