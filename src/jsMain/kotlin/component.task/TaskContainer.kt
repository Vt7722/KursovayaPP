package component.task

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
import ru.altmanea.webapp.data.Lesson
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

val containerTaskList = FC("QueryTaskList") { _: Props ->
    val queryClient = useQueryClient()
    val taskListQueryKey = arrayOf("TaskList").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = taskListQueryKey,
        queryFn = {
            fetchText(
                Config.tasksPath,
                jso {
                    headers = json("Authorization" to userInfo?.second?.authHeader)
                }
            )
        }
    )

    val addTaskMutation = useMutation<HTTPResult, Any, Task, Any>(
        mutationFn = { task: Task ->
            fetch(
                Config.tasksPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(task)
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(taskListQueryKey)
            }
        }
    )


    if (query.isLoading) div { +"Loading .." }
    else if (query.isError) div { +"Error!" }
    else {
        val items =
            Json.decodeFromString<Triple<List<Task>,List<Lesson>,List<Group>>>(query.data ?: "")
        CAddTask{
            lessons = items.second
            groups = items.third
            addTask = {
                addTaskMutation.mutateAsync(it, null)
            }
        }
        CRemoveTask{}
        CTaskList {
            tasks = items.first
        }
    }
}