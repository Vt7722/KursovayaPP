package ru.altmanea.webapp.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import ru.altmanea.webapp.access.User

class AuthConfig {
    companion object {
        val secret = "secret"
        val issuer = "http://0.0.0.0:8080/"
        val audience = "http://0.0.0.0:8080/hello"
        val myRealm = "Access to 'hello'"
    }
}

fun Application.authConfig() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = AuthConfig.myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(AuthConfig.secret))
                    .withAudience(AuthConfig.audience)
                    .withIssuer(AuthConfig.issuer)
                    .build()
            )
            validate { credential ->
                userList.find {
                    it.username == credential.payload.getClaim("username").asString()
                }?.let {
                    UserPrincipal(it)
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    install(Authorization) {
        getRole = { user ->
            val user = userList.find { it.username == user.username }
            userRoles.getOrDefault(user, emptySet())
        }
    }
}