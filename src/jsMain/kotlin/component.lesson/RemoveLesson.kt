package component.lesson

import QueryError
import csstype.px
import emotion.react.css
import invalidateRepoKey
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
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
import web.html.HTMLInputElement
import web.html.HTMLSelectElement
import kotlin.js.json

external interface LessonSelectProps : Props {
    var startName: String
    var onNoPick: (String) -> Unit
}

val CLessonSelect = FC<LessonSelectProps>("LessonSelect") { props ->
    val selectQueryKey = arrayOf("LessonSelect", props.startName).unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = selectQueryKey,
        queryFn = {
            fetchText(
                "${Config.lessonsPath}ByStartName/${props.startName}",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )
    val selectRef = useRef<HTMLSelectElement>()
    val lessons: List<Lesson> =
        try {
            Json.decodeFromString(query.data ?: "")
        } catch (e: Throwable) {
            emptyList()
        }
    select {
        css{
            height = 21.px
        }
        ref = selectRef
        lessons.map {
            option {
                +it.name
                value = it._id
            }
        }
    }
    button{
        +"Удалить"
        onClick = {
            selectRef.current?.value?.let {
                props.onNoPick(it)
            }
        }
    }
}

val CRemoveLesson = FC<Props>("RemoveLesson") {_ ->
    val queryClient = useQueryClient()
    val invalidateRepoKey = useContext(invalidateRepoKey)
    var input by useState("")
    val inputRef = useRef<HTMLInputElement>()
    val userInfo = useContext(userInfoContext)
    val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
        { lessonId: String->
            fetch(
                "${Config.lessonsPath}lessonDelete/$lessonId",
                jso {
                    method = "DELETE"
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(invalidateRepoKey)
            }
        }
    )


    div {
        css{
            marginLeft = 42.px
        }
        input {
            ref = inputRef
            onChange = { input = it.target.value }
        }
        CLessonSelect {
            startName = input
            onNoPick = {
                deleteMutation.mutateAsync(it, null)
            }
        }
    }
}
