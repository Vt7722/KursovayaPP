package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Student(
    val firstname: String,
    val surname: String,
    val group: String,
    val _id: String,
    val version: Long
) {
    fun fullname() =
        "$firstname $surname"

    fun fullNameWithGroup() =
        "$firstname $surname $group"
}

val Student.json
    get() = Json.encodeToString(this)

