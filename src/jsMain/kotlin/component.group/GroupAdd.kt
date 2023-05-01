package component.group

import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span
import react.useRef
import ru.altmanea.webapp.data.Group
import web.html.HTMLInputElement

external interface AddGroupProps : Props {
    var addGroup: (Group) -> Unit
}

val CAddGroup= FC<AddGroupProps>("AddGroup") { props ->
    val groupRef = useRef<HTMLInputElement>()
    div {
        css{
            marginTop = 15.px
        }
        span {
            label { +"Группа: " }
            input { ref = groupRef }
        }
        button {
            +"Добавить"
            onClick = {
                groupRef.current?.value?.let { group ->
                    val regex = "[1-9][0-9][а-я]".toRegex()
                    if(group.matches(regex)) {
                        props.addGroup(Group(group, emptyList(), "", 0))
                    }
                }
            }
        }
    }
}
