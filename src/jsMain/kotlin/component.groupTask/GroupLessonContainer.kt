package component.groupTask

import QueryError
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.router.Params
import react.router.useParams
import react.useContext
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Task
import tanstack.query.core.QueryKey
import tanstack.react.query.useQuery
import tools.fetchText
import userInfoContext
import kotlin.js.json

val containerLessonGroupList = FC("QueryLessonGroupList") { _: Props ->
    val params: Params = useParams()
    val lessonId = params["lesson"]
    val groupId = params["group"]
    val lessonGroupListQueryKey = arrayOf("LessonGroupList").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = lessonGroupListQueryKey,
        queryFn = {
            fetchText(
                "${Config.lessonsPath}$lessonId/$groupId",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<Triple<String,String,List<Task>>>(query.data ?: "")
        CTaskListGroup {
            tasks = items.third
            group = items.first
            lesson = items.second
        }
    }
}
