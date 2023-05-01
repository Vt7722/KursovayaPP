package ru.altmanea.webapp.command.commandLesson

import kotlinx.serialization.Serializable

@Serializable
class ComAddGroupToLesson (
    val lessonId: String,
    val groupId: String,
    val version: Long
) {
    companion object {
        const val path="addGroup"
    }
}