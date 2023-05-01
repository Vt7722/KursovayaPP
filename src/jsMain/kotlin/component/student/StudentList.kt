package component.student

import react.FC
import react.Props
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.dom.Link
import ru.altmanea.webapp.data.Student

external interface StudentListProps : Props {
    var students: List<Student>
}

val CStudentList = FC<StudentListProps>("StudentList") { props ->
    ol {
        props.students.forEach { student->
            li {
                Link{
                    +student.fullNameWithGroup()
                    to = student._id
                }
            }
        }
    }
}
