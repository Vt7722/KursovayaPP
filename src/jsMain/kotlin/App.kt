
import auth.authProvider
import component.group.containerGroupList
import component.group.pageGroupContainer
import component.groupTask.containerLessonGroupList
import component.groupTask.containerLessonGroupTask
import component.lesson.containerLessonList
import component.lesson.pageLessonContainer
import component.student.pageStudentContainer
import component.student.studentContainer
import component.task.containerTaskList
import component.task.pageTaskContainer
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.create
import react.createContext
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
import ru.altmanea.webapp.access.Token
import ru.altmanea.webapp.access.User
import ru.altmanea.webapp.config.Config
import tanstack.query.core.QueryClient
import tanstack.query.core.QueryKey
import tanstack.react.query.QueryClientProvider
import web.dom.document

typealias  UserInfo = Pair<User, Token>?

val invalidateRepoKey = createContext<QueryKey>()
val userInfoContext = createContext<UserInfo>(null)

fun main() {
    val container = document.getElementById("root")!!
    createRoot(container).render(app.create())
}

val app = FC<Props>("App") {
    HashRouter {
        authProvider {
            QueryClientProvider {
                client = QueryClient()
                div {
                    Link {
                        css {
                            background = Color("#fff44f")
                            padding = Padding(vertical = 10.px, horizontal = 10.px)
                            border = Border(width = 2.px, style = LineStyle.solid)
                            borderRadius = 10.pc
                            margin= Margin(30.px, 0.px)
                        }
                        +"Группы"
                        to = Config.groupsPath
                    }
                    Link {
                        css {
                            background = Color("#fff44f")
                            padding = Padding(vertical = 10.px, horizontal = 10.px)
                            border = Border(width = 2.px, style = LineStyle.solid)
                            borderRadius = 10.pc
                        }
                        +"Предметы"
                        to = Config.lessonsPath
                    }
                    Link {
                        css {
                            background = Color("#fff44f")
                            padding = Padding(vertical = 10.px, horizontal = 10.px)
                            border = Border(width = 2.px, style = LineStyle.solid)
                            borderRadius = 10.pc
                        }
                        +"Задания"
                        to = Config.tasksPath
                    }
                    Link {
                        css {
                            background = Color("#fff44f")
                            padding = Padding(vertical = 10.px, horizontal = 10.px)
                            border = Border(width = 2.px, style = LineStyle.solid)
                            borderRadius = 10.pc
                        }
                        +"Добавить студента"
                        to = Config.studentsPath
                    }
                }
                Routes {
                    //группа
                    Route {
                        path = Config.groupsPath
                        element = containerGroupList.create()
                    }
                    //Урок
                    Route {
                        path = Config.lessonsPath
                        element = containerLessonList.create()
                    }
                    //студенты
                    Route {
                        path = Config.studentsPath
                        element = studentContainer.create()
                    }
                    //задания
                    Route {
                        path = Config.tasksPath
                        element = containerTaskList.create()
                    }
                    //Страница задания
                    Route {
                        path = Config.tasksPath + ":id"
                        element = pageTaskContainer.create()
                    }
                    //Страница группы
                    Route {
                        path = Config.groupsPath + ":id"
                        element = pageGroupContainer.create()
                    }
                    //Страница урока
                    Route {
                        path = Config.lessonsPath + ":lesson"
                        element = pageLessonContainer.create()
                    }
                    //Страница cтудента
                    Route {
                        path = Config.studentsPath + ":id"
                        element = pageStudentContainer.create()
                    }
                    //Страница урок-группа
                    Route {
                        path = "${Config.lessonsPath}:lesson/:group"
                        element = containerLessonGroupList.create()
                    }
                    Route {
                        path = "${Config.lessonsPath}:lesson/:group/:task"
                        element = containerLessonGroupTask.create()
                    }
                }
            }
        }
    }
}