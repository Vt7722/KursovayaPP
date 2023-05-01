import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.altmanea.webapp.common.Item
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Student
import ru.altmanea.webapp.data.json
import ru.altmanea.webapp.main
import ru.altmanea.webapp.repo.studentsRepo


class ApplicationTest : StringSpec({
    "Students routes" {
        testApplication {
            application {
                main()
            }
            val response = client.get("/students/")
            response.status shouldBe HttpStatusCode.OK
            val stud = Json.decodeFromString<List<Item<Student>>>(response.bodyAsText()).apply {
                size shouldBe 4
            }
            /*val students = withClue("read") {
                val response = client.get("/students/")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<List<Item<Student>>>(response.bodyAsText()).apply {
                    size shouldBe 4
                }
            }
            withClue("read id") {
                val sheldon = students.first { it.elem.firstname == "Sheldon" }
                val response = client.get("/students/${sheldon.id}")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<Item<Student>>(response.bodyAsText()).apply {
                    elem.firstname shouldBe "Sheldon"
                }
            }
            val newStudents = withClue("create") {
                val response = client.post("/students/") {
                    contentType(ContentType.Application.Json)
                    setBody(Student("Raj", "Koothrappali", "20m").json)
                }
                response.status shouldBe HttpStatusCode.Created
                Json.decodeFromString<List<Item<Student>>>(
                    client.get("/students/").bodyAsText()
                ).apply {
                    size shouldBe 5
                }
            }
            val emi = withClue("update") {
                val raj = newStudents.first { it.elem.firstname == "Raj" }
                client.put("/students/${raj.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(Student("Amy", "Fowler", "20k").json)
                }
                Json.decodeFromString<Item<Student>>(
                    client.get("/students/${raj.id}").bodyAsText()
                ).apply {
                    elem.firstname shouldBe "Amy"
                }
            }
            withClue("delete"){
                client.delete("/students/${emi.id}")
                Json.decodeFromString<List<Item<Student>>>(
                    client.get("/students/").bodyAsText()
                ).apply {
                    size shouldBe 4
                }
            }*/
            withClue("getBody") {
                val group = "20k"
                val studentInGroup = stud.filter { it.elem.group == group }
                val testStudents = Json.decodeFromString<List<Item<Student>>>(client.get(Config.groupsBody) {
                    contentType(ContentType.Application.Json)
                    setBody(group)
                }.bodyAsText())
                studentInGroup[0].id shouldBe testStudents[0].id
                studentInGroup[1].id shouldBe testStudents[1].id
            }
        }
    }
})

