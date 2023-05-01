package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Group(
    val name: String,
    val students: List<Student>,
    val _id: String,
    val version: Long
){
    fun addGroup(studentI: Student)=
        Group(
            name,
            students+studentI,
            _id,
            version
        )
}

val Group.json
    get() = Json.encodeToString(this)