package component.task

import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.textarea
import react.useRef
import ru.altmanea.webapp.data.GradeInfo
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Lesson
import ru.altmanea.webapp.data.Task
import web.html.HTMLInputElement
import web.html.HTMLSelectElement
import web.html.HTMLTextAreaElement
import web.html.InputType

external interface AddTaskProps : Props {
    var lessons: List<Lesson>
    var groups: List<Group>
    var addTask: (Task) -> Unit
}

val CAddTask = FC<AddTaskProps>("AddTask") { props ->
    val lessonRef = useRef<HTMLSelectElement>()
    val groupRef = useRef<HTMLSelectElement>()
    val typeRef = useRef<HTMLSelectElement>()
    val dateRef = useRef<HTMLInputElement>()
    val dedlineRef = useRef<HTMLInputElement>()
    val taskRef = useRef<HTMLTextAreaElement>()
    val types = listOf("Лекция", "Практика", "Лабораторная")

    div {
        css {
            marginTop = 15.px
        }
        div {
            label {
                css {
                    marginRight = 80.px
                }
                +"Урок: "
            }
            select {
                css {
                    width = 114.px
                }
                ref = lessonRef
                props.lessons.map {
                    option {
                        +it.name
                        value = it.name
                    }
                }
            }
        }
        div {
            label {
                css {
                    marginRight = 66.px
                }
                +"Группа: "
            }
            select {
                css {
                    width = 114.px
                }
                ref = groupRef
                props.groups.map {
                    option {
                        +it.name
                        value = it.name
                    }
                }
            }
        }
        div {
            label {
                css {
                    marginRight = 30.px
                }
                +"Вид занятия: "
            }
            select {
                css {
                    width = 114.px
                }
                ref = typeRef
                types.map {
                    option {
                        +it
                        value = it
                    }
                }
            }
        }
        div {
            label {
                +"Дата добавления: "
            }
            input {
                ref = dateRef
                type = InputType.date
            }
        }
        div {
            label {
                css {
                    marginRight = 40.px
                }
                +"Дата сдачи: "
            }
            input {
                ref = dedlineRef
                type = InputType.date
            }
        }
        div {
            label {
                css {
                    marginRight = 59.px
                    verticalAlign = VerticalAlign.top
                }
                +"Задание: "
            }
            textarea { ref = taskRef }
            button {
                css{
                    verticalAlign = VerticalAlign.top
                }
                +"Добавить"
                onClick = {
                    lessonRef.current?.value?.let { lesson ->
                        groupRef.current?.value?.let { group ->
                            typeRef.current?.value?.let { type ->
                                dateRef.current?.value?.let { date ->
                                    dedlineRef.current?.value?.let { dedline ->
                                        taskRef.current?.value?.let { task ->
                                            val gr = props.groups.find { it.name == group }
                                            val newGrd = mutableListOf<GradeInfo>()
                                            gr!!.students.map {
                                                newGrd.add(GradeInfo(it, null))
                                            }
                                            props.addTask(
                                                Task(
                                                    lesson, group, type, date, dedline, task, newGrd, "", 0
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
