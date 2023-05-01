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
import ru.altmanea.webapp.auth.authorization
import ru.altmanea.webapp.auth.roleAdmin
import ru.altmanea.webapp.command.commandStudent.ComChangeStud
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Student
import ru.altmanea.webapp.repo.studentDb
import java.util.*

fun Route.studentRoutes() {
    route(Config.studentsPath) {
        authenticate("auth-jwt") {
            authorization(setOf(roleAdmin)) {
                get {
                    val students = studentDb.find().toList() as List<Student>
                    if (students.isEmpty())
                        return@get call.respondText("No students found", status = HttpStatusCode.NotFound)
                    call.respond(students)
                }
                post {
                    val student = call.receive<Student>()
                    val studId = Student(
                        student.firstname, student.surname, student.group,
                        UUID.randomUUID().toString(), System.currentTimeMillis()
                    )
                    if (studentDb.find(
                            and(
                                Student::firstname eq student.firstname, Student::surname eq student.surname
                            )
                        ).firstOrNull() != null
                    )
                        return@post call.respondText(
                            "The student already exists", status = HttpStatusCode.BadRequest
                        )
                    studentDb.insertOne(studId)
                    call.respondText(
                        "Student stored correctly", status = HttpStatusCode.Created
                    )
                }
                get("ByStartName/{startName}") {
                    val startName =
                        call.parameters["startName"] ?: return@get call.respondText(
                            "Missing or malformed startName", status = HttpStatusCode.BadRequest
                        )
                    val students = studentDb.find().filter {
                        it.firstname.startsWith(startName)
                    } as List<Student>
                    if (students.isEmpty())
                        return@get call.respondText("No students found", status = HttpStatusCode.NotFound)
                    call.respond(students)
                }
                delete("/studentDelete/{idS}") {
                    val id = call.parameters["idS"] ?: return@delete call.respondText(
                        "Student not found", status = HttpStatusCode.BadRequest
                    )
                    studentDb.deleteOne(Student::_id eq id)
                    call.respondText(
                        "Student deleted correctly", status = HttpStatusCode.OK
                    )
                }
                get("{id}") {
                    val id =
                        call.parameters["id"] ?: return@get call.respondText(
                            "Missing or malformed id", status = HttpStatusCode.BadRequest
                        )
                    val student = studentDb.find(Student::_id eq id).firstOrNull()
                        ?: return@get call.respondText(
                            "No student with id $id", status = HttpStatusCode.NotFound
                        )
                    call.respond(student)
                }
                put("{id}/${ComChangeStud.path}") {
                    val command = Json.decodeFromString(ComChangeStud.serializer(), call.receive())
                    val student = studentDb.find(Student::_id eq command.studentId).firstOrNull() as Student
                    if (command.version != student.version)
                        call.respondText(
                            "Student had updated on server", status = HttpStatusCode.BadRequest
                        )
                    val newVersionStudent = Student(
                        command.student.firstname, command.student.surname,
                        command.student.group, command.student._id,
                        System.currentTimeMillis()
                    )
                    studentDb.replaceOne(Student::_id eq command.studentId, newVersionStudent)
                    call.respondText(
                        "Student updates correctly", status = HttpStatusCode.Created
                    )
                }
            }
        }
    }
}