package component.group

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
import ru.altmanea.webapp.data.Group
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

external interface GroupSelectProps : Props {
    var startName: String
    var onNoPick: (String) -> Unit
}

val CGroupSelect = FC<GroupSelectProps>("GroupSelect") { props ->
    val selectQueryKey = arrayOf("GroupSelectRemove", props.startName).unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = selectQueryKey,
        queryFn = {
            fetchText(
                "${Config.groupsPath}ByStartName/${props.startName}",
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                })
        }
    )
    val selectRef = useRef<HTMLSelectElement>()
    val groups: List<Group> =
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
        groups.map {
            option {
                +it.name
                value = it._id
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


val CRemoveGroup = FC<Props>("AddStudent") { _ ->
    val queryClient = useQueryClient()
    val invalidateRepoKey = useContext(invalidateRepoKey)
    var input by useState("")
    val inputRef = useRef<HTMLInputElement>()
    val userInfo = useContext(userInfoContext)
    val deleteMutation = useMutation<HTTPResult, Any, String, Any>(
        { id: String ->
            fetch(
                "${Config.groupsPath}groupDelete/$id",
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
            marginLeft = 57.px
        }
        input {
            ref = inputRef
            onChange = { input = it.target.value }
        }
        CGroupSelect {
            startName = input
            onNoPick = {
                deleteMutation.mutateAsync(it, null)
            }
        }
    }
}
