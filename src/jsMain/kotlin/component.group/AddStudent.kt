package component.group

import QueryError
import csstype.px
import emotion.react.css
import invalidateRepoKey
import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import ru.altmanea.webapp.command.commandGroup.ComAddStudToGroup
import ru.altmanea.webapp.command.commandGroup.ComDelStudFromGroup
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

external interface StudentSelectProps : Props {
    var startName: String
    var onPick: (String) -> Unit
    var onNoPick: (String) -> Unit
}

val CStudentSelect = FC<StudentSelectProps>("StudentSelect") { props ->
    val selectQueryKey = arrayOf("StudentSelectAdd", props.startName).unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = selectQueryKey,
        queryFn = {
            fetchText(
                "${Config.studentsPath}ByStartName/${props.startName}",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )
    val selectRef = useRef<HTMLSelectElement>()
    val students: List<Student> =
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
        students.map {
            option {
                +it.fullname()
                value = it._id
            }
        }
    }
    button {
        +"Добавить"
        onClick = {
            selectRef.current?.value?.let {
                props.onPick(it)
            }
        }
    }
    button {
        +"Удалить"
        onClick = {
            selectRef.current?.value?.let {
                props.onNoPick(it)
            }
        }
    }
}

external interface AddStudentProps : Props {
    var groupAddStudent: Group
}

val CAddStudentToGroup = FC<AddStudentProps>("AddStudent") { props ->
    val queryClient = useQueryClient()
    val invalidateRepoKey = useContext(invalidateRepoKey)
    var input by useState("")
    val inputRef = useRef<HTMLInputElement>()
    val userInfo = useContext(userInfoContext)

    val addStudentMutation = useMutation<HTTPResult, Any, String, Any>(
        mutationFn = { studentId: String ->
            fetch(
                "${Config.groupsPath}${props.groupAddStudent._id}/${ComAddStudToGroup.path}",
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(
                        ComAddStudToGroup(
                            studentId,
                            props.groupAddStudent._id,
                            props.groupAddStudent.version
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

    val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
        { studentId: String ->
            fetch(
                "${Config.groupsPath}${props.groupAddStudent._id}/${ComDelStudFromGroup.path}",
                jso {
                    method = "DELETE"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(
                        ComDelStudFromGroup(
                            studentId,
                            props.groupAddStudent._id,
                            props.groupAddStudent.version
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
    div {
        +"Студент: "
        input {
            ref = inputRef
            onChange = {
                input = it.target.value
            }
        }
        CStudentSelect {
            startName = input.capitalize()
            onPick = {
                addStudentMutation.mutateAsync(it, null)
            }
            onNoPick = {
                deleteMutation.mutateAsync(it, null)
            }
        }
    }
}
