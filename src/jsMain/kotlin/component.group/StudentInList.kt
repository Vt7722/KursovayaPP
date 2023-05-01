package component.group

import react.FC
import react.Props
import react.dom.html.ReactHTML.span
import ru.altmanea.webapp.data.Student

external interface StudentInListProps : Props {
    var student: Student
}

val CStudentInList = FC<StudentInListProps>("StudentInList") { props ->
    span {
        +props.student.fullname()
    }
}
