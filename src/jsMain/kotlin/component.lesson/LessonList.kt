package component.lesson

import react.FC
import react.Props
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.router.dom.Link
import ru.altmanea.webapp.data.Lesson


external interface LessonListProps : Props {
    var lessons: List<Lesson>
}


val CLessonList = FC<LessonListProps>("GroupList") { props ->
    ol {
        props.lessons.forEach { lesson ->
            li {
                Link{
                    +lesson.name
                    to = lesson._id
                }
            }
        }
    }
}