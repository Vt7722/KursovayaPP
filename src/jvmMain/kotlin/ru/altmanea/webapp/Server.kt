package ru.altmanea.webapp

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import ru.altmanea.webapp.auth.AuthException
import ru.altmanea.webapp.auth.authConfig
import ru.altmanea.webapp.auth.authRoutes
import ru.altmanea.webapp.repo.createTestData
import ru.altmanea.webapp.rest.groupRoutes
import ru.altmanea.webapp.rest.lessonRoutes
import ru.altmanea.webapp.rest.studentRoutes
import ru.altmanea.webapp.rest.tasksRoutes


fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "127.0.0.1",
        watchPaths = listOf("classes")
    ) {
        main()
    }.start(wait = true)
}

fun Application.main(isTest: Boolean = true) {
    config(isTest)
    static()
    rest()
    if (isTest) logRoute()
}

fun Application.config(isTest: Boolean) {
    install(StatusPages) {
        exception<AuthException> { call, cause ->
            cause.handler(call)
        }
    }
    install(ContentNegotiation) {
        json()
    }
    authConfig()
    if (isTest) {
        createTestData()
        /*install(createApplicationPlugin("DelayEmulator") {
            onCall {
                delay(0L)
            }
        })*/
    }
}

fun Application.rest() {
    routing {
        authRoutes()
        studentRoutes()
        groupRoutes()
        lessonRoutes()
        tasksRoutes()
    }
}