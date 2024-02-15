package app

import java.io.File
import java.util.concurrent.TimeUnit

private fun runCommand(
    workingDir: File = File("."),
    prompt: String,
): String? = runCatching {
    ProcessBuilder(
        listOf(
            "./main",
            "-m",
            "./models/llama-2-7b-chat/ggml-model-Q4_K_M.gguf",
            "-t",
            "8",
            "-n",
            "512",
            "-p",
            prompt
        )
    )
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(60, TimeUnit.SECONDS) }
        .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()


data class CreateElementResponse(
    val element: Element?,
    val logs: List<String>,
)

fun createElement(elements: List<Element>): CreateElementResponse {
    val logs = mutableListOf<String>()
    val elementsString = elements.joinToString(" and ") { it.name }
    val prompt = """
        You are a crafting game that generates new existing elements by combining 2 or more elements. 
        For example combining water and fire makes steam, combining water and wind makes a wave, 
        combining earth and wave makes sand. Reply with your first suggestion only and provide an emoji 
        for the icon using the following format: "{emoji} - {name}". What different thing does combining
        $elementsString make?
    """.trimIndent().split("\n").joinToString(" ")

    logs.add("Prompt: $prompt")

    val result =
        runCommand(File("../llama.cpp"), prompt) ?: return CreateElementResponse(null, logs + "Response: Empty")

    val response = result.split("?").last()
    logs.add("Response: $response")

    val responseSplit = response.split("-").map { it.trim() }
    val icon = responseSplit.firstOrNull() ?: return CreateElementResponse(null, logs)
    val name = responseSplit.lastOrNull() ?: return CreateElementResponse(null, logs)
    if (icon.isBlank() || name.isBlank()) return CreateElementResponse(null, logs)

    return CreateElementResponse(Element(icon, name), logs)
}