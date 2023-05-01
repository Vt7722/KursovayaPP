package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Lesson(
    val name: String,
    val groups: List<Group>,
    val tasks: List<Task>,
    val _id: String,
    val version: Long
){
    fun addGroup(groupI: Group)=
        Lesson(
            name,
            groups+groupI,
            tasks,
            _id,
            version
        )
}

val Lesson.json
    get() = Json.encodeToString(this)