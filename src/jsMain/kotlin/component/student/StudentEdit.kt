package component.student

import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.useRef
import ru.altmanea.webapp.data.Student
import web.html.HTMLInputElement

external interface EditStudentProps : Props {
    var oldStudent: Student
    var saveStudent: (Student) -> Unit
}

val CEditStudent = FC<EditStudentProps>("Edit student") { props ->
    val firstnameRef = useRef<HTMLInputElement>()
    val surnameRef = useRef<HTMLInputElement>()
    span {
        input {
            placeholder = "Имя"
            ref = firstnameRef
        }
        input {
            placeholder = "Фамилия"
            ref = surnameRef
        }
    }
    button {
        +"✓"
        onClick = {
            firstnameRef.current?.value?.let { firstname ->
                surnameRef.current?.value?.let { surname ->
                    val regex = "^[А-ЯЁ][а-яё]*\$".toRegex()
                    if (firstname.matches(regex) && surname.matches(regex)) {
                        props.saveStudent(Student(firstname, surname, props.oldStudent.group, props.oldStudent._id, 0))
                    }
                }
            }
        }
    }
}
