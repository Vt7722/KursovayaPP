package component.group


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
import react.router.useParams
import react.useContext
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Group
import tanstack.query.core.QueryKey
import tanstack.react.query.useQuery
import tools.fetchText
import userInfoContext
import kotlin.js.json

val pageGroupContainer = FC("PageGroupContainer") { _: Props ->
    val params: Params = useParams()
    val groupNumber = params["id"]
    val pageGroupQueryKey = arrayOf("idGroup").unsafeCast<QueryKey>()
    val userInfo = useContext(userInfoContext)
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = pageGroupQueryKey,
        queryFn = {
            fetchText(
                Config.groupsPath + groupNumber,
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
            Json.decodeFromString<Group>(query.data ?: "")
        CPageGroup {
            group = item
        }
    }
}


external interface PageGroupProps : Props {
    var group: Group

}

val CPageGroup = FC<PageGroupProps>("PageGroup") { props ->
    h1{
        +props.group.name
    }
    CAddStudentToGroup{
        groupAddStudent = props.group
    }
    //компонент трансфер
    CTransferStudentGroup{
        group = props.group
    }

    ol {
        props.group.students.forEach {
            li {
                CStudentInList {
                    student = it
                }
            }
        }
    }
}
