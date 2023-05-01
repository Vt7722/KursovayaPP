package component.group

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
import ru.altmanea.webapp.data.Group
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val containerGroupList = FC("QueryGroupList") { _: Props ->
    val queryClient = useQueryClient()
    val groupListQueryKey = arrayOf("GroupList").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = groupListQueryKey,
        queryFn = {
            fetchText(
                Config.groupsPath,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    val addGroupMutation = useMutation<HTTPResult, Any, Group, Any>(
        mutationFn = { group: Group ->
            fetch(
                Config.groupsPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader)
                    body = Json.encodeToString(
                        group
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(groupListQueryKey)
            }
        }
    )


    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<List<Group>>(query.data ?: "")
        CAddGroup {
            addGroup = {
                addGroupMutation.mutateAsync(it, null)
            }
        }
        CRemoveGroup{}
        CGroupList {
            groups = items
        }
    }
}