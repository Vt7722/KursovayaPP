package component.task


import QueryError
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.textarea
import react.dom.html.ReactHTML.tr
import react.router.Params
import react.router.useParams
import react.useContext
import react.useRef
import ru.altmanea.webapp.command.commandTask.ComChangeTask
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Task
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement
import web.html.InputType
import kotlin.js.json

val pageTaskContainer = FC("PageTaskContainer") { _: Props ->
    val queryClient = useQueryClient()
    val params: Params = useParams()
    val taskId = params["id"]
    val pageTaskQueryKey = arrayOf("idTask").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = pageTaskQueryKey,
        queryFn = {
            fetchText(
                Config.tasksPath + taskId,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    val refactorTaskMutation = useMutation<HTTPResult, Any, Triple<String, String, Long>, Any>(
        mutationFn = { refactor: Triple<String, String, Long> ->
            fetch(
                Config.tasksPath + taskId + "/" + ComChangeTask.path,
                jso {
                    method = "PUT"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(
                        ComChangeTask(
                            taskId.toString(),
                            refactor.first,
                            refactor.second,
                            refactor.third
                        )
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(pageTaskQueryKey)
            }
        }
    )

    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val item =
            Json.decodeFromString<Task>(query.data ?: "")
        CPageTask {
            task = item
            refactorTask = {
                refactorTaskMutation.mutateAsync(it, null)
            }
        }
    }
}


external interface PageTaskProps : Props {
    var task: Task
    var refactorTask: (Triple<String, String, Long>) -> Unit
}

val CPageTask = FC<PageTaskProps>("PageTask") { props ->
    val dedlineRef = useRef<HTMLInputElement>()
    val taskRef = useRef<HTMLTextAreaElement>()
    h1 {
        +"${props.task.date} ${props.task.groupTask} ${props.task.lessonTask}"
    }
    div {
        label {
            +"Дата сдачи: "
        }
        input {
            ref = dedlineRef
            type = InputType.date
        }
    }
    div {
        label {
            css {
                marginRight = 23.px
                verticalAlign = VerticalAlign.top
            }
            +"Задание:"
        }
        textarea { ref = taskRef }
        button {
            css {
                verticalAlign = VerticalAlign.top
            }
            +"Добавить"
            onClick = {
                dedlineRef.current?.value?.let { dedline ->
                    taskRef.current?.value?.let { task ->
                        props.refactorTask(
                            Triple(dedline, task, props.task.version)
                        )
                    }
                }
            }
        }
    }


    div {
        table {
            tbody {
                tr {
                    td { +"Урок" }
                    td { +props.task.lessonTask }
                }
                tr {
                    td { +"Группа" }
                    td { +props.task.groupTask }
                }
                tr {
                    td { +"Тип занятия" }
                    td { +props.task.type }
                }
                tr {
                    td { +"Дата добавления" }
                    td { +props.task.date }
                }
                tr {
                    td { +"Дата сдачи" }
                    td { +props.task.dedline }
                }
                tr {
                    td { +"Задание" }
                    td { +props.task.task }
                }
            }
        }
    }
}
