package ru.altmanea.webapp.command.commandGroup

import kotlinx.serialization.Serializable

@Serializable
class ComTransfer (
    val studentId: String,
    val currentGroupId: String,
    val transferGroupId: String,
    val version: Long
) {
    companion object {
        const val path="transfer"
    }
}