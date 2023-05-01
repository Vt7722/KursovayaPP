package ru.altmanea.webapp.command.commandStudent

import kotlinx.serialization.Serializable
import ru.altmanea.webapp.data.Student

@Serializable
class ComChangeStud (
    val studentId: String,
    val student: Student,
    val version: Long
) {
    companion object {
        const val path="changeStudent"
    }
}