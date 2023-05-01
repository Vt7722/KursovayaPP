package component.groupTask

import react.FC
import react.Props
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.dom.Link
import ru.altmanea.webapp.data.Task


external interface TaskListGroupProps : Props {
    var tasks: List<Task>
    var group: String
    var lesson: String
}


val CTaskListGroup = FC<TaskListGroupProps>("TaskListGroup") { props ->
    h1 {
        +props.group
    }
    h2 {
        +props.lesson
    }
    ol {
        props.tasks.forEach { task ->
            li {
                Link {
                    +task.date
                    to = task._id
                }
            }
        }
    }
}
