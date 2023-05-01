package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.altmanea.webapp.config.Grade

@Serializable
class Task(
    val lessonTask: String,
    val groupTask: String,
    val type: String,
    val date: String,
    val dedline: String,
    val task: String,
    val grade: List<GradeInfo>,
    val _id: String,
    val version: Long
) {}
@Serializable
class GradeInfo(
    val student: Student,
    val grade: Grade?
)
{
    fun newGrade(grade: Grade?) =
        GradeInfo(student, grade)
}
val Task.json
    get() = Json.encodeToString(this)