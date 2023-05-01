package ru.altmanea.webapp.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import ru.altmanea.webapp.auth.authorization
import ru.altmanea.webapp.auth.roleAdmin
import ru.altmanea.webapp.auth.roleTeacher
import ru.altmanea.webapp.command.commandTask.ComChangeTask
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Task
import ru.altmanea.webapp.repo.groupDb
import ru.altmanea.webapp.repo.lessonDb
import ru.altmanea.webapp.repo.taskDb
import java.time.LocalDate
import java.util.*

fun Route.tasksRoutes() {
    route(Config.tasksPath) {
        authenticate("auth-jwt") {
            authorization(setOf(roleAdmin, roleTeacher)) {
                get {
                    val tasks = taskDb.find().toList() as List<Task>
                    val lessons = lessonDb.find().toList().map { it }
                    val grp = groupDb.find().toList().map {
                        if (it.students.isNotEmpty())
                            it
                        else null
                    }
                    val groups = groupDb.find().toList().map { it } as List<Group>
                    if (lessons.isEmpty() || groups.isEmpty())
                        return@get call.respondText(
                            "No groups/lessons found", status = HttpStatusCode.NotFound
                        )
                    call.respond(Triple(tasks, lessons, grp.filterNotNull()))
                }
                post {
                    val task = call.receive<Task>()
                    val localdate = LocalDate.parse(task.date)
                    val localdedline = LocalDate.parse(task.dedline)
                    val taskId = Task(
                        task.lessonTask, task.groupTask, task.type,
                        task.date, task.dedline, task.task,
                        task.grade, UUID.randomUUID().toString(),
                        System.currentTimeMillis()
                    )
                    if (taskDb.find(
                            and(
                                Task::date eq task.date,
                                Task::lessonTask eq task.lessonTask,
                                Task::groupTask eq task.groupTask,
                                Task::type eq task.type
                            )
                        ).firstOrNull() != null
                    )
                        return@post call.respondText(
                            "The task already exists", status = HttpStatusCode.BadRequest
                        )
                    if(localdedline.isAfter(localdate)) {
                        taskDb.insertOne(taskId)
                        call.respondText(
                            "Task stored correctly", status = HttpStatusCode.Created
                        )
                    }
                }
                get("ByStartName/{startName}") {
                    val startName =
                        call.parameters["startName"] ?: return@get call.respondText(
                            "Missing or malformed startName", status = HttpStatusCode.BadRequest
                        )
                    val tasks = taskDb.find().filter {
                        it.date.startsWith(startName) ||
                                it.groupTask.startsWith(startName) ||
                                it.lessonTask.startsWith(startName)
                    } as List<Task>
                    if (tasks.isEmpty())
                        return@get call.respondText(
                            "Task not found", status = HttpStatusCode.NotFound
                        )
                    call.respond(tasks)
                }
                delete("/taskDelete/{idT}") {
                    val id = call.parameters["idT"] ?: return@delete call.respondText(
                        "Missing or malformed task id", status = HttpStatusCode.BadRequest
                    )
                    taskDb.deleteOne(Task::_id eq id)
                    call.respondText(
                        "Task deleted correctly", status = HttpStatusCode.OK
                    )
                }
                get("{id}") {
                    val id =
                        call.parameters["id"] ?: return@get call.respondText(
                            "Missing or malformed id", status = HttpStatusCode.BadRequest
                        )
                    val task = taskDb.find(Task::_id eq id).firstOrNull()
                        ?: return@get call.respondText(
                            "No task with id $id", status = HttpStatusCode.NotFound
                        )
                    call.respond(task)
                }
                put("{id}/${ComChangeTask.path}") {
                    val command = Json.decodeFromString(ComChangeTask.serializer(), call.receive())
                    val task = taskDb.find(Task::_id eq command.taskId).firstOrNull() as Task
                    val localdedline = LocalDate.parse(task.dedline)
                    val localdedlineNew = LocalDate.parse(command.taskDedline)
                    if (command.version != task.version)
                        call.respondText(
                            "Task had updated on server", status = HttpStatusCode.BadRequest
                        )
                    if(localdedlineNew.isAfter(localdedline)) {
                        taskDb.updateOne(
                            Task::_id eq command.taskId,
                            setValue(Task::dedline, command.taskDedline)
                        )
                        taskDb.updateOne(Task::_id eq command.taskId, setValue(Task::task, command.task))
                        taskDb.updateOne(
                            Task::_id eq command.taskId,
                            setValue(Task::version, System.currentTimeMillis())
                        )
                        call.respondText(
                            "Task updates correctly", status = HttpStatusCode.Created
                        )
                    }
                }
            }
        }
    }
}

