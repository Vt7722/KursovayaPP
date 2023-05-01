package auth

import csstype.pt
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState
import ru.altmanea.webapp.access.User
import web.html.InputType

typealias Username = String
typealias Password = String

external interface AuthInProps : Props {
    var signIn: (Username, Password) -> Unit
}

external interface AuthOutProps : Props {
    var user: User
    var signOff: () -> Unit
}

val CAuthIn = FC<AuthInProps>("Auth") { props ->
    var name by useState("")
    var pass by useState("")
    div{
        label{
            css {
                marginRight=8.px
            }
            +"Логин: "
        }
        input {
            type = InputType.text
            value = name
            onChange = { name = it.target.value }
        }
    }
    div {
        label {
            +"Пароль: "
        }
        input {
            type = InputType.text
            value = pass
            onChange = { pass = it.target.value }
        }
        button {
            +"Войти"
            onClick = {
                props.signIn(name, pass)
            }
        }
    }

}

val CAuthOut = FC<AuthOutProps>("Auth") { props ->
    div {
        css{
            marginBottom = 20.px
            marginLeft = 10.px
            fontSize = 12.pt
        }
        +"${props.user.username} "
        button {
            +"Выход"
            onClick = {
                props.signOff()
            }
        }
    }
}