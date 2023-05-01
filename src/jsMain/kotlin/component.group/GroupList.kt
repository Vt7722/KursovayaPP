package component.group

import react.FC
import react.Props
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.dom.Link
import ru.altmanea.webapp.data.Group


external interface GroupListProps : Props {
    var groups: List<Group>
}


val CGroupList = FC<GroupListProps>("GroupList") { props ->
    ol {
        props.groups.forEach { group ->
            li {
                Link {
                    +group.name
                    to = group._id
                }
            }
        }
    }
}