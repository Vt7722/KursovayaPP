package ru.altmanea.webapp.auth

import ru.altmanea.webapp.access.Role
import ru.altmanea.webapp.access.User

val userAdmin = User("admin","admin")
val userTeacher = User("teacher", "teacher")
val userStudent = User("student", "student")
val userList = listOf(userAdmin, userTeacher, userStudent)

val roleAdmin = Role("admin")
val roleTeacher = Role("teacher")
val roleUser = Role("user")
val roleList = listOf(roleAdmin, roleTeacher ,roleUser)

val userRoles = mapOf(
    userAdmin to setOf(roleAdmin, roleTeacher ,roleUser),
    userTeacher to setOf(roleTeacher, roleUser),
    userStudent to setOf(roleUser)
)