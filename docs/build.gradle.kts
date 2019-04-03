import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.MULTILINE

tasks {
    create("build") {
        doFirst {
            processMarkDown(project.projectDir.resolve("src"))
        }
    }
}

fun processMarkDown(dir: File) {
    dir.listFiles().filter { it.name.endsWith("template.md") }.forEach { file ->
        processMarkDown(file, file.parentFile.resolve("../" + file.name.replace(".template", "")))
    }
}

fun processMarkDown(src: File, dest: File) {
    val text = src.readText()
    val expandedText = expandCodeBlocks(text)
    dest.writeText(headerFor(src) + expandedText)
}

fun Iterable<String>.withoutPreamble(): List<String> = this
    .filter { !it.startsWith("import") && !it.startsWith("package") }
    .dropWhile { it.isEmpty() }

fun linesFrom(filename: String) = File(filename).readLines()

fun headerFor(file: File) = if (file.name.startsWith("README.")) "" else header

val codeBlockFinder = "^```insert-kotlin (.*?)^```".toRegex(setOf(DOT_MATCHES_ALL, MULTILINE))
private val header = "[Minutest](README.md)\n\n"

fun expandCodeBlocks(text: String): String =
    codeBlockFinder.replace(text) { matchResult ->
        matchResult.groups[1]?.value?.let { filename ->
            (listOf("```kotlin") +
                linesFrom(filename.trim()).withoutPreamble() +
                "```" +
                """<small>\[[$filename](../$filename)\]</small>"""
                ).joinToString("\n")
        } ?: error("No filename found for kotlin block")
    }