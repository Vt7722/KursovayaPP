package component.groupTask

import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import ru.altmanea.webapp.data.GradeInfo
import ru.altmanea.webapp.data.Task


external interface TaskGroupProps : Props {
    var task: Task
    var changeStudents: (Pair<List<GradeInfo>, Long>) -> Unit
}


val CTaskGroup = FC<TaskGroupProps>("TaskListGroup") { props ->
    h2{
        +"${props.task.lessonTask} ${props.task.groupTask}"
    }
    p{
        +props.task.type
        br{}
        +"Дата выдачи: ${props.task.date}"
        br{}
        b{
           +"Дата сдачи: ${props.task.dedline}"
        }
        br{}
        +"Задание: ${props.task.task}"
    }

    table{
        tbody{
            props.task.grade.map { grade ->
                tr {
                    td {
                        +grade.student.fullname()
                    }
                    td {
                       CGrade {
                            init = grade.grade
                            change = { newGrade ->
                                val newStudents = props.task.grade.map {
                                    if (it.student._id == grade.student._id)
                                        it.newGrade(newGrade)
                                    else
                                        it
                                }
                                props.changeStudents(Pair(newStudents, props.task.version))
                            }
                        }
                    }
                }
            }
        }
    }
}
