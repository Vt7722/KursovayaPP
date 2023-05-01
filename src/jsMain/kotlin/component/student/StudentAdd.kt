package component.student

import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useRef
import ru.altmanea.webapp.data.Student
import web.html.HTMLInputElement

external interface AddStudentProps : Props {
    var addStudent: (Student) -> Unit
}

val CAddStudent = FC<AddStudentProps>("AddStudent") { props ->
    val firstnameRef = useRef<HTMLInputElement>()
    val surnameRef = useRef<HTMLInputElement>()
    div {
        css{
            marginTop = 15.px
        }
        div {
            label {
                css{
                    marginRight = 33.px
                }
                +"Имя: "
            }
            input { ref = firstnameRef }
        }
        div {
            label { +"Фамилия: " }
            input { ref = surnameRef }
            button {
                +"Добавить"
                onClick = {
                    firstnameRef.current?.value?.let { firstname ->
                        surnameRef.current?.value?.let { surname ->
                            val regex = "^[А-ЯЁ][а-яё]*\$".toRegex()
                            if (firstname.matches(regex) && surname.matches(regex)) {
                                props.addStudent(Student(firstname, surname, "", "", 0))
                            }
                        }
                    }
                }
            }
        }
    }
}
