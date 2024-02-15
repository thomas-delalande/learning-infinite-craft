package app

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.p
import view.index

fun main() {
    embeddedServer(Netty, port = 8087) {
        routes()
    }.start(wait = true)
}


fun Application.routes() {
    routing {
        get {
            call.respondHtml {
                index {
                    page()
                }
            }
        }

        post("/select/{index}") {
            val index = call.parameters["index"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (selected.contains(index)) selected.remove(index) else selected.add(index)
            call.respondHtml {
                body {
                    page()
                }
            }
        }
        post("/craft") {
            craft()
            call.respondHtml {
                body {
                    page()
                }
            }
        }
    }
}

fun FlowContent.page() = div {
    div {
        id = "page"
        classes = setOf("flex", "gap-4", "p-5")

        div {
            classes = setOf("flex", "gap-4", "p-5", "flex-wrap", "h-fit")
            button {
                attributes["hx-post"] = "/craft"
                attributes["hx-target"] = "#page"
                attributes["hx-swap"] = "outerHTML"

                classes = setOf("px-4", "py-2", "rounded-md", "bg-green-300", "w-80", "disabled:bg-neutral-500")
                disabled = selected.size < 2
                +"Craft"
            }

            elements.mapIndexed { index, it ->
                element(it, selected.contains(index), index, new == index)
            }

        }
        div {
            classes = setOf("max-w-lg")
            logs.map {
                p {
                    classes = setOf("text-black", "bg-neutral-200", "rounded-lg", "p-4", "my-4")
                    +it
                }
            }
        }
    }
}

val elements = mutableListOf(
    Element("💧", "Water"),
    Element("🔥", "Fire"),
    Element("🪨", "Earth"),
    Element("💨", "Air"),
)

val selected = mutableListOf<Int>()
var new: Int? = null

fun FlowContent.element(element: Element, selected: Boolean, index: Int, new: Boolean) = button {
    classes = setOf(
        "px-4",
        "py-2",
        "rounded-md",
        "w-80"
    ) + if (selected) "bg-blue-300" else if (new) "bg-yellow-300" else "bg-neutral-300"
    attributes["hx-post"] = "/select/$index"
    attributes["hx-target"] = "#page"
    attributes["hx-swap"] = "outerHTML"

    +"${element.icon} ${element.name}"
}

data class Element(val icon: String, val name: String)

fun craft() {
    new = 0
    val selectedElements = selected.map { elements[it] }
    val result = createElement(selectedElements)
    result.element?.let { element ->
        if (elements.none { it.name == element.name })
            elements.add(0, element)
    }
    logs.addAll(0, result.logs)
    selected.clear()
}

val logs = mutableListOf<String>()