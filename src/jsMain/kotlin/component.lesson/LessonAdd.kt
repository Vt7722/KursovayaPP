package component.lesson

import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useRef
import ru.altmanea.webapp.data.Lesson
import web.html.HTMLInputElement

external interface AddLessonProps : Props {
    var addLesson: (Lesson) -> Unit
}

val CAddLesson= FC<AddLessonProps>("AddLesson") { props ->
    val lessonRef = useRef<HTMLInputElement>()
    div {
        css{
            marginTop = 15.px
        }
        label { +"Урок: " }
        input { ref = lessonRef }
        button {
            +"Добавить"
            onClick = {
                lessonRef.current?.value?.let { lesson ->
                    val regex = "^[А-ЯЁ][а-яё]*\$".toRegex()
                    if(lesson.matches(regex)) {
                        props.addLesson(Lesson(lesson, emptyList(), emptyList(), "", 0))
                    }
                }
            }
        }
    }
}
