package component.student

import QueryError
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useContext
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Student
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val studentContainer = FC("StudentsContainer") { _: Props ->
    val queryClient = useQueryClient()
    val studentListQueryKey = arrayOf("studentList").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = studentListQueryKey,
        queryFn = {
            fetchText(
                Config.studentsPath,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    val addStudentMutation = useMutation<HTTPResult, Any, Student, Any>(
        mutationFn = { student: Student ->
            fetch(
                Config.studentsPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                        )
                    body = Json.encodeToString(student)
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(studentListQueryKey)
            }
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<List<Student>>(query.data ?: "")
        CAddStudent {
            addStudent = {
                addStudentMutation.mutateAsync(it, null)
            }
        }
        CRemoveStudent{}
        CStudentList {
            students = items
        }
    }
}
