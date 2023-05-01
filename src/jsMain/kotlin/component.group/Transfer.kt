package component.group

import QueryError
import csstype.px
import emotion.react.css
import invalidateRepoKey
import js.core.get
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import react.router.Params
import react.router.useParams
import ru.altmanea.webapp.command.commandGroup.ComTransfer
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Student
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

external interface TransferSelectProps : Props {
    var startNameStudent: String
    var startNameGroup: String
    var onPick: (Pair<String, String>) -> Unit
}

val CTransferSelect = FC<TransferSelectProps>("TransferSelect") { props ->
    val selectQueryKeyStudent = arrayOf("TransferSelectStudent", props.startNameStudent).unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val queryStudent = useQuery<String, QueryError, String, QueryKey>(
        queryKey = selectQueryKeyStudent,
        queryFn = {
            fetchText(
                "${Config.studentsPath}ByStartName/${props.startNameStudent}",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                })
        }
    )
    val selectRefStudent = useRef<HTMLSelectElement>()
    val students: List<Student> =
        try {
            Json.decodeFromString(queryStudent.data ?: "")
        } catch (e: Throwable) {
            emptyList()
        }
    div {
        select {
            css{
                height = 21.px
            }
            ref = selectRefStudent
            students.map {
                ReactHTML.option {
                    +it.fullname()
                    value = it._id
                }
            }
        }
    }

    val selectQueryKeyGroup = arrayOf("TransferSelectGroup", props.startNameGroup).unsafeCast<QueryKey>()
    val queryGroup = useQuery<String, QueryError, String, QueryKey>(
        queryKey = selectQueryKeyGroup,
        queryFn = {
            fetchText(
                "${Config.groupsPath}ByStartName/${props.startNameGroup}",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                })
        }
    )
    val selectRefGroup = useRef<HTMLSelectElement>()
    val groups: List<Group> =
        try {
            Json.decodeFromString(queryGroup.data ?: "")
        } catch (e: Throwable) {
            emptyList()
        }
    div {
        select {
            css{
                height = 21.px
            }
            ref = selectRefGroup
            groups.map {
                option {
                    +it.name
                    value = it._id
                }
            }
        }
        button {
            +"Перевести"
            onClick = {
                selectRefStudent.current?.value?.let { student ->
                    selectRefGroup.current?.value?.let { group ->
                        props.onPick(Pair(group, student))
                    }
                }
            }
        }
    }
}

external interface TransferProps : Props {
    var group: Group
}

val CTransferStudentGroup = FC<TransferProps>("TransferStudent") { props ->
    val queryClient = useQueryClient()
    val invalidateRepoKey = useContext(invalidateRepoKey)
    var inputStudent by useState("")
    val inputRefStudent = useRef<HTMLInputElement>()
    var inputGroup by useState("")
    val inputRefGroup = useRef<HTMLInputElement>()
    val params: Params = useParams()
    val groupNumber = params["id"]
    val userInfo = useContext(userInfoContext)
    val transferMutation = useMutation<HTTPResult, Any, Pair<String, String>, Any>(
        mutationFn = { pair: Pair<String, String> ->
            fetch(
                Config.groupsPath + groupNumber + "/" + ComTransfer.path,
                jso {
                    method = "PUT"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(
                        ComTransfer(
                            pair.second,
                            props.group._id,
                            pair.first,
                            props.group.version
                        )
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(invalidateRepoKey)
            }
        }
    )
    label{+"Перевод"}
    table {
        css{
            borderSpacing = 0.px
            border = 0.px
        }
        tbody {
            tr {
                td {
                    div {
                        label{+"Студент: "}
                        input {
                            ref = inputRefStudent
                            onChange = { inputStudent = it.target.value }
                        }
                    }
                    div {
                        label{
                            css{
                                marginRight = 7.px
                            }
                            +"Группа: "
                        }
                        input {
                            ref = inputRefGroup
                            onChange = { inputGroup = it.target.value }
                        }
                    }
                }
                td {
                    span {
                        CTransferSelect {
                            startNameStudent = inputStudent
                            startNameGroup = inputGroup
                            onPick = {
                                transferMutation.mutateAsync(it, null)
                            }
                        }
                    }
                }
            }
        }
    }
}


