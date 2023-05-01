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
import ru.altmanea.webapp.command.commandGroup.ComAddStudToGroup
import ru.altmanea.webapp.command.commandGroup.ComDelStudFromGroup
import ru.altmanea.webapp.command.commandGroup.ComTransfer
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Student
import ru.altmanea.webapp.repo.groupDb
import ru.altmanea.webapp.repo.studentDb
import java.util.*

fun Route.groupRoutes() {
    route(Config.groupsPath) {
        authenticate("auth-jwt") {
            authorization(setOf(roleAdmin, roleTeacher, roleUser)) {
                get {
                    val groups = groupDb.find().toList() as List<Group>
                    if (groups.isEmpty())
                        return@get call.respondText(
                            "Groups not found", status = HttpStatusCode.NotFound
                        )
                    call.respond(groups)
                }
                get("ByStartName/{startName}") {
                    val startName = call.parameters["startName"] ?: return@get call.respondText(
                        "Missing or malformed startName", status = HttpStatusCode.BadRequest
                    )
                    val groups = groupDb.find().filter { it.name.startsWith(startName) } as List<Group>
                    if (groups.isEmpty())
                        return@get call.respondText(
                            "Groups not found", status = HttpStatusCode.NotFound
                        )
                    call.respond(groups)
                }
                get("{id}") {
                    val id = call.parameters["id"]
                        ?: return@get call.respondText(
                            "Missing or malformed id", status = HttpStatusCode.BadRequest
                        )
                    val group = groupDb.find(Group::_id eq id).firstOrNull()
                        ?: return@get call.respondText(
                            "No group with id $id", status = HttpStatusCode.NotFound
                        )
                    call.respond(group)
                }
                authorization(setOf(roleAdmin)) {
                    post {
                        val group = call.receive<Group>()
                        val groupId = Group(
                            group.name, group.students,
                            UUID.randomUUID().toString(),
                            System.currentTimeMillis()
                        )
                        if (groupDb.find(Group::name eq group.name).firstOrNull() != null)
                            return@post call.respondText(
                                "The group already exists", status = HttpStatusCode.BadRequest
                            )
                        groupDb.insertOne(groupId)
                        call.respondText(
                            "Group stored correctly", status = HttpStatusCode.Created
                        )
                    }
                    delete("/groupDelete/{idG}") {
                        val id = call.parameters["idG"]
                            ?: return@delete call.respondText(
                                "Missing or malformed id", status = HttpStatusCode.BadRequest
                            )
                        val group = groupDb.find(Group::_id eq id).firstOrNull()
                            ?: return@delete call.respondText(
                                "Group not found", status = HttpStatusCode.NotFound
                            )
                        studentDb.updateMany(Student::group eq group.name, setValue(Student::group, ""))
                        studentDb.updateMany(
                            Student::group eq group.name,
                            setValue(Student::version, System.currentTimeMillis())
                        )
                        groupDb.deleteOne(Group::_id eq id)
                        call.respondText(
                            "Group deleted correctly", status = HttpStatusCode.OK
                        )
                    }
                    //add/remove student
                    post("{idG}/${ComAddStudToGroup.path}") {
                        val command =
                            Json.decodeFromString(ComAddStudToGroup.serializer(), call.receive())// добавление студента
                        val group = groupDb.find(Group::_id eq command.groupId).firstOrNull()
                            ?: return@post call.respondText(
                                "No group with id ${command.groupId}", status = HttpStatusCode.NotFound
                            )
                        //id студента
                        val student = studentDb.find(Student::_id eq command.studentId).firstOrNull()
                            ?: return@post call.respondText(
                                "No student with id ${command.studentId}", status = HttpStatusCode.NotFound
                            )
                        if (command.version != group.version)
                            call.respondText(
                                "Group had updated on server", status = HttpStatusCode.BadRequest
                            )
                        //есть ли студент на уроке уже есть
                        groupDb.find().toList().forEach {
                            if (it.students.find { it._id == command.studentId } != null) {
                                return@post call.respondText(
                                    "Student already in group", status = HttpStatusCode.BadRequest
                                )
                            }
                        }
                        studentDb.updateOne(Student::_id eq command.studentId, setValue(Student::group, group.name))
                        studentDb.updateOne(
                            Student::_id eq command.studentId,
                            setValue(Student::version, System.currentTimeMillis())
                        )
                        val newStud = group.addGroup(student)
                        groupDb.replaceOne(Group::_id eq command.groupId, newStud)
                        groupDb.updateOne(
                            Group::_id eq command.groupId,
                            setValue(Group::version, System.currentTimeMillis())
                        )
                        call.respondText(
                            "Student added correctly", status = HttpStatusCode.OK
                        )
                    }
                    delete("{idG}/${ComDelStudFromGroup.path}") {
                        val command = Json.decodeFromString(ComDelStudFromGroup.serializer(), call.receive())
                        val group = groupDb.find(Group::_id eq command.groupId).firstOrNull()
                            ?: return@delete call.respondText(
                                "Group not found", status = HttpStatusCode.NotFound
                            )
                        if (command.version != group.version)
                            call.respondText(
                                "Group had updated on server", status = HttpStatusCode.BadRequest
                            )
                        val students = group.students.filter { it._id != command.studentId }
                        groupDb.updateOne(Group::_id eq command.groupId, setValue(Group::students, students))
                        groupDb.updateOne(
                            Group::_id eq command.groupId,
                            setValue(Group::version, System.currentTimeMillis())
                        )
                        studentDb.updateOne(Student::_id eq command.studentId, setValue(Student::group, ""))
                        studentDb.updateOne(
                            Student::_id eq command.studentId,
                            setValue(Student::version, System.currentTimeMillis())
                        )
                        call.respondText(
                            "Student deleted correctly", status = HttpStatusCode.OK
                        )
                    }
                    //transfer
                    put("{id}/${ComTransfer.path}") {
                        val command = Json.decodeFromString(ComTransfer.serializer(), call.receive())
                        // перевод студента
                        val transferStudent = studentDb.find(Student::_id eq command.studentId).firstOrNull()
                            ?: return@put call.respondText(
                                "Transfer student not found", status = HttpStatusCode.NotFound
                            )
                        val transferGroup = groupDb.find(Group::_id eq command.transferGroupId).firstOrNull()
                            ?: return@put call.respondText(
                                "Transfer group not found", status = HttpStatusCode.NotFound
                            )
                        val curGr = groupDb.find(Group::_id eq command.currentGroupId).firstOrNull()
                            ?: return@put call.respondText(
                                "Current group not found", status = HttpStatusCode.NotFound
                            )
                        if (command.version != curGr.version)
                            call.respondText(
                                "Group had updated on server", status = HttpStatusCode.BadRequest
                            )
                        if (transferGroup.students.contains(transferStudent))
                            return@put call.respondText(
                                "Student already in group", status = HttpStatusCode.BadRequest
                            )
                        val studTrans = transferGroup.students.plus(transferStudent)//Добавили студента в группу новую
                        val studentsAfterTransfer =
                            groupDb.find(Group::_id eq command.currentGroupId).firstOrNull()!!
                                .students.filter { it._id != command.studentId }
                        groupDb.updateOne(Group::_id eq transferGroup._id, setValue(Group::students, studTrans))
                        groupDb.updateOne(
                            Group::_id eq transferGroup._id,
                            setValue(Group::version, System.currentTimeMillis())
                        )
                        groupDb.updateOne(Group::_id eq curGr._id, setValue(Group::students, studentsAfterTransfer))
                        groupDb.updateOne(Group::_id eq curGr._id, setValue(Group::version, System.currentTimeMillis()))
                        studentDb.updateOne(
                            Student::_id eq transferStudent._id,
                            setValue(Student::group, transferGroup.name)
                        )//перезаписапли группу у студента
                        call.respondText(
                            "Transfer correctly", status = HttpStatusCode.Created
                        )
                    }
                }
            }
        }
    }
}


