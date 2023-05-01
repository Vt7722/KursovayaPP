package component.lesson

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
import ru.altmanea.webapp.data.Lesson
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val containerLessonList = FC("QueryLessonList") { _: Props ->
    val queryClient = useQueryClient()
    val lessonListQueryKey = arrayOf("LessonList").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = lessonListQueryKey,
        queryFn = {
            fetchText(
                Config.lessonsPath,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    val addLessonMutation = useMutation<HTTPResult, Any, Lesson, Any>(
        mutationFn = { lesson: Lesson ->
            fetch(
                Config.lessonsPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(lesson)
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(lessonListQueryKey)
            }
        }
    )


    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<List<Lesson>>(query.data ?: "")
        CAddLesson {
            addLesson = {
                addLessonMutation.mutateAsync(it, null)
            }
        }
        CRemoveLesson{}
        CLessonList {
            lessons = items
        }
    }
}