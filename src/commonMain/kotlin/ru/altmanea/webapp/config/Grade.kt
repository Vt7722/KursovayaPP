package ru.altmanea.webapp.config

import kotlinx.serialization.Serializable


@Serializable
enum class Grade (val mark: Int){
    A(5),
    B(4),
    C(3),
    F(2);

    companion object {
        val list = listOf(A, B, C, F)
    }
}