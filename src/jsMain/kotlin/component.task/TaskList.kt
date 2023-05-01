package component.task

import react.FC
import react.Props
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.dom.Link
import ru.altmanea.webapp.data.Task


external interface TaskListProps : Props {
    var tasks: List<Task>
}


val CTaskList = FC<TaskListProps>("TaskList") { props ->
    ol {
        props.tasks.forEach { task ->
            li {
                Link {
                    +"${task.date} ${task.groupTask} ${task.lessonTask}"
                    to = task._id
                }
            }
        }
    }
}