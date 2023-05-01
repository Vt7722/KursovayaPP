package ru.altmanea.webapp.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import ru.altmanea.webapp.auth.authorization
import ru.altmanea.webapp.auth.roleAdmin
import ru.altmanea.webapp.auth.roleTeacher
import ru.altmanea.webapp.auth.roleUser
import ru.altmanea.webapp.command.commandLesson.ComAddGroupToLesson
import ru.altmanea.webapp.command.commandLesson.ComDelGroupFromLesson
import ru.altmanea.webapp.command.commandLesson.ComSetGrade
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Lesson
import ru.altmanea.webapp.data.Task
import ru.altmanea.webapp.repo.groupDb
import ru.altmanea.webapp.repo.lessonDb
import ru.altmanea.webapp.repo.taskDb
import java.util.*

fun Route.lessonRoutes() {
    route(Config.lessonsPath) {
        authenticate("auth-jwt") {
            authorization(setOf(roleAdmin, roleTeacher, roleUser)) {
                get {
                    val lessons = lessonDb.find().toList() as List<Lesson>
                    if (lessons.isEmpty()) return@get call.respondText(
                        "No lesson found",
                        status = HttpStatusCode.NotFound
                    )
                    call.respond(lessons)
                }
                get("ByStartName/{startName}") {
                    val startName = call.parameters["startName"] ?: return@get call.respondText(
                        "Missing or malformed startName", status = HttpStatusCode.BadRequest
                    )
                    val lessons = lessonDb.find().filter {
                        it.name.startsWith(startName)
                    } as List<Lesson>
                    if (lessons.isEmpty()) return@get call.respondText(
                        "No lessons found",
                        status = HttpStatusCode.NotFound
                    )
                    call.respond(lessons)
                }
                get("{id}") {
                    val id = call.parameters["id"] ?: return@get call.respondText(
                        "Missing or malformed id", status = HttpStatusCode.BadRequest
                    )
                    val lesson = lessonDb.find(Lesson::_id eq id).firstOrNull() ?: return@get call.respondText(
                        "No lesson with id $id", status = HttpStatusCode.NotFound
                    )
                    call.respond(lesson)
                }
                get("{idL}/{idG}") {
                    val idL = call.parameters["idL"] ?: return@get call.respondText(
                        "Missing or malformed lesson id", status = HttpStatusCode.BadRequest
                    )
                    val idG = call.parameters["idG"] ?: return@get call.respondText(
                        "Missing or malformed group id", status = HttpStatusCode.BadRequest
                    )
                    val lesson = lessonDb.find(Lesson::_id eq idL).firstOrNull() ?: return@get call.respondText(
                        "No lesson with id $idL", status = HttpStatusCode.NotFound
                    )
                    val group = lesson.groups.find { it._id == idG } ?: return@get call.respondText(
                        "No group with id $idL", status = HttpStatusCode.NotFound
                    )
                    val task = taskDb.find().toList().filter { it.lessonTask == lesson.name }
                        .filter { it.groupTask == group.name }
                    call.respond(Triple(group.name, lesson.name, task))
                }
                get("{idL}/{idG}/{idT}") {
                    val idT = call.parameters["idT"] ?: return@get call.respondText(
                        "Missing or malformed task id", status = HttpStatusCode.BadRequest
                    )
                    val task = taskDb.find(Task::_id eq idT).firstOrNull() ?: return@get call.respondText(
                        "No task with id $idT", status = HttpStatusCode.NotFound
                    )
                    call.respond(task)
                }
                authorization(setOf(roleAdmin, roleTeacher)) {
                    put("{idL}/{idG}/${ComSetGrade.path}") {
                        val command = Json.decodeFromString(ComSetGrade.serializer(), call.receive())
                        val task = taskDb.find(Task::_id eq command.taskId).firstOrNull() as Task
                        if (command.version != task.version) call.respondText(
                            "Task had updated on server", status = HttpStatusCode.BadRequest
                        )
                        taskDb.updateOne(Task::_id eq command.taskId, setValue(Task::grade, command.grade))
                        taskDb.updateOne(
                            Task::_id eq command.taskId, setValue(Task::version, System.currentTimeMillis())
                        )
                        call.respondText(
                            "Task updates correctly", status = HttpStatusCode.Created
                        )
                    }
                    authorization(setOf(roleAdmin)) {
                        post {
                            val lesson = call.receive<Lesson>()
                            val lessonId = Lesson(
                                lesson.name,
                                lesson.groups,
                                lesson.tasks,
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                            )
                            if (lessonDb.find(Lesson::name eq lesson.name)
                                    .firstOrNull() != null
                            ) return@post call.respondText(
                                "The lesson already exists", status = HttpStatusCode.BadRequest
                            )
                            lessonDb.insertOne(lessonId)
                            call.respondText(
                                "Lesson stored correctly", status = HttpStatusCode.Created
                            )
                        }
                        delete("/lessonDelete/{idL}") {
                            val id = call.parameters["idL"] ?: return@delete call.respondText(
                                "Missing or malformed lesson id", status = HttpStatusCode.BadRequest
                            )
                            lessonDb.deleteOne(Lesson::_id eq id)
                            call.respondText(
                                "Lesson deleted correctly", status = HttpStatusCode.OK
                            )
                        }
                        post("{idL}/${ComAddGroupToLesson.path}") {
                            val command = Json.decodeFromString(ComAddGroupToLesson.serializer(), call.receive())
                            val lesson = lessonDb.find(Lesson::_id eq command.lessonId).firstOrNull()
                                ?: return@post call.respondText(
                                    "No lesson with id ${command.lessonId}", status = HttpStatusCode.NotFound
                                )
                            val group = groupDb.find(Group::_id eq command.groupId).firstOrNull()
                                ?: return@post call.respondText(
                                    "No group with id ${command.groupId}", status = HttpStatusCode.NotFound
                                )
                            if (command.version != lesson.version) call.respondText(
                                "Lesson had updated on server", status = HttpStatusCode.BadRequest
                            )
                            lessonDb.find(Lesson::_id eq command.lessonId).toList().forEach {
                                if (it.groups.find { it._id == command.groupId } != null) return@post call.respondText(
                                    "Group already in lesson", status = HttpStatusCode.BadRequest
                                )
                            }
                            val newGroup = lesson.addGroup(group)
                            lessonDb.replaceOne(Lesson::_id eq command.lessonId, newGroup)
                            lessonDb.updateOne(
                                Lesson::_id eq command.lessonId, setValue(Lesson::version, System.currentTimeMillis())
                            )
                            call.respondText(
                                "Group added correctly", status = HttpStatusCode.OK
                            )
                        }
                        delete("{idL}/${ComDelGroupFromLesson.path}") {
                            val command = Json.decodeFromString(ComDelGroupFromLesson.serializer(), call.receive())
                            val lesson = lessonDb.find(Lesson::_id eq command.lessonId).firstOrNull()
                                ?: return@delete call.respondText(
                                    "No lesson with id ${command.lessonId}", status = HttpStatusCode.NotFound
                                )
                            if (command.version != lesson.version) call.respondText(
                                "Lesson had updated on server", status = HttpStatusCode.BadRequest
                            )
                            val groups = lesson.groups.filter { it._id != command.groupId }
                            lessonDb.updateOne(Lesson::_id eq command.lessonId, setValue(Lesson::groups, groups))
                            lessonDb.updateOne(
                                Lesson::_id eq command.lessonId, setValue(Lesson::version, System.currentTimeMillis())
                            )
                            call.respondText(
                                "Group deleted correctly", status = HttpStatusCode.OK
                            )
                        }
                    }
                }
            }
        }
    }
}