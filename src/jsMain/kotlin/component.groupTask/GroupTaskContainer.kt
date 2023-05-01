package component.groupTask

import QueryError
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.router.Params
import react.router.useParams
import react.useContext
import ru.altmanea.webapp.command.commandLesson.ComSetGrade
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.GradeInfo
import ru.altmanea.webapp.data.Task
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val containerLessonGroupTask = FC("QueryLessonGroupTask") { _: Props ->
    val queryClient = useQueryClient()
    val params: Params = useParams()
    val lessonId = params["lesson"]
    val groupId = params["group"]
    val taskId = params["task"]
    val lessonGroupTaskQueryKey = arrayOf("LessonGroupTask").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = lessonGroupTaskQueryKey,
        queryFn = {
            fetchText(
                "${Config.lessonsPath}$lessonId/$groupId/$taskId",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                })
        }
    )
    val updateGradeMutation = useMutation<HTTPResult, Any, Pair<List<GradeInfo>, Long>, Any>(
        mutationFn = { grade: Pair<List<GradeInfo>, Long> ->
            fetch(
                "${Config.lessonsPath}$lessonId/$groupId/${ComSetGrade.path}",
                jso {
                    method = "PUT"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader)
                    body = Json.encodeToString(
                        ComSetGrade(
                            taskId.toString(),
                            grade.first,
                            grade.second
                        )
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonGroupTaskQueryKey)
            }
        }
    )
    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<Task>(query.data ?: "")
        CTaskGroup {
            task = items
            changeStudents = {
                updateGradeMutation.mutateAsync(it, null)
            }
        }
    }
}
