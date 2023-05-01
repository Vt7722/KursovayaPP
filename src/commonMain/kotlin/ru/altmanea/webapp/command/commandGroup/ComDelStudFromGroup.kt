package ru.altmanea.webapp.command.commandGroup

import kotlinx.serialization.Serializable

@Serializable
class ComDelStudFromGroup (
    val studentId: String,
    val groupId: String,
    val version: Long
) {
    companion object {
        const val path="deleteStudent"
    }
}