package ru.altmanea.webapp.command.commandLesson

import kotlinx.serialization.Serializable
import ru.altmanea.webapp.data.GradeInfo

@Serializable
class ComSetGrade (
    val taskId: String,
    val grade: List<GradeInfo>,
    val version: Long
) {
    companion object {
        const val path="setGrade"
    }
}