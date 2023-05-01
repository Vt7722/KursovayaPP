package ru.altmanea.webapp.command.commandTask

import kotlinx.serialization.Serializable

@Serializable
class ComChangeTask (
    val taskId: String,
    val taskDedline: String,
    val task: String,
    val version: Long
) {
    companion object {
        const val path="changeTask"
    }
}