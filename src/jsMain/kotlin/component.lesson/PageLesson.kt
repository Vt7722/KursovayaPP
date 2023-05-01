package component.lesson


import QueryError
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.Params
import react.router.dom.Link
import react.router.useParams
import react.useContext
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Lesson
import tanstack.query.core.QueryKey
import tanstack.react.query.useQuery
import tools.fetchText
import userInfoContext
import kotlin.js.json

val pageLessonContainer = FC("PageLessonContainer") { _: Props ->
    val params: Params = useParams()
    val lessonId = params["lesson"]
    val pageGroupQueryKey = arrayOf("lessonPage").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = pageGroupQueryKey,
        queryFn = {
            fetchText(
                Config.lessonsPath + lessonId,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val item =
            Json.decodeFromString<Lesson>(query.data ?: "")
        CPageLesson {
            lesson = item
        }
    }
}


external interface PageLessonProps : Props {
    var lesson: Lesson
}

val CPageLesson = FC<PageLessonProps>("PageLesson") { props ->
    h1{
        +props.lesson.name
    }
    CAddGroupToLesson{
        lessonAddGroup = props.lesson
    }
    ol {
        props.lesson.groups.forEach {
            li {
                Link{
                    +it.name
                    to = it._id
                }
            }
        }
    }
}
