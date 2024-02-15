package view

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title

fun HTML.index(page: FlowContent.() -> Unit) {
    classes = setOf("h-full", "bg-gray-100")
    head {
        title {
            +"Infinite Craft Copy"
        }
        script { src = "https://unpkg.com/htmx.org@1.9.10" }
        script { src = "https://cdn.tailwindcss.com" }
    }
    body {
        div {
            id = "content"
            page()
        }
    }
}