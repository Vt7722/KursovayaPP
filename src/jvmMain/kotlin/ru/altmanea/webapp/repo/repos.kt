package ru.altmanea.webapp.repo

import org.litote.kmongo.getCollection
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Lesson
import ru.altmanea.webapp.data.Student
import ru.altmanea.webapp.data.Task
import ru.altmanea.webapp.mongoDatabase
import java.util.*

val studentDb = mongoDatabase.getCollection<Student>().apply { drop() }
val taskDb = mongoDatabase.getCollection<Task>().apply { drop() }
val groupDb = mongoDatabase.getCollection<Group>().apply { drop() }
val lessonDb = mongoDatabase.getCollection<Lesson>().apply { drop() }

fun createTestData() {

    listOf(
        Student("Мария", "Козлова", "", UUID.randomUUID().toString(), System.currentTimeMillis()),
        Student("Андрей", "Иванов", "", UUID.randomUUID().toString(), System.currentTimeMillis()),
        Student("Екатерина", "Соколова", "", UUID.randomUUID().toString(), System.currentTimeMillis()),
        Student("Дмитрий", "Петров", "", UUID.randomUUID().toString(), System.currentTimeMillis()),
    ).apply {
        map {
            studentDb.insertOne(it)
        }
    }
    listOf(
        Group("20л", emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis()),
        Group("29з", emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis()),
        Group("20м", emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis())
    ).apply {
        map {
            groupDb.insertOne(it)
        }
    }
    listOf(
        Lesson("Математика", emptyList(), emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis()),
        Lesson("Физика", emptyList(), emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis()),
        Lesson("История", emptyList(), emptyList(), UUID.randomUUID().toString(), System.currentTimeMillis())
    ).apply {
        map {
            lessonDb.insertOne(it)
        }
    }
}
