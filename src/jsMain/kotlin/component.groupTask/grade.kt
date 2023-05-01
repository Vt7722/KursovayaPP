package component.groupTask

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import ru.altmanea.webapp.config.Grade

external interface GradeProps : Props {
    var init: Grade?
    var change: (Grade?) -> Unit
}

val CGrade = FC<GradeProps> ("Grade") { props ->
    select {
        option {
            value = "null"
        }
        Grade.list.map {
            option {
                +"${it.mark}"
                value = Json.encodeToString(it)
            }
        }
        defaultValue = Json.encodeToString(props.init)
        onChange = {
            props.change(Json.decodeFromString(it.target.value))
        }
    }
}