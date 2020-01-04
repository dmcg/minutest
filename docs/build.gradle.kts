import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.MULTILINE

tasks {
    create("build") {
        doFirst {
            processMarkDown(project.projectDir)
        }
    }
}

fun processMarkDown(dir: File) {
    dir.listFiles().filter { it.name.endsWith(".md") }.forEach { file ->
        processMarkDown(file, file)
    }
}

fun processMarkDown(src: File, dest: File) {
    val text = src.readText()
    dest.writeText(expandCodeBlocks(text))
}

fun Iterable<String>.withoutPreamble(): List<String> = this
    .filter { !it.startsWith("import") && !it.startsWith("package") }
    .dropWhile { it.isEmpty() }

val root = project.projectDir

fun expandCodeBlocks(text: String): String =
    expandedCodeBlockFinder.replace(text) { matchResult ->
        matchResult.groups[1]?.value?.let { filename ->
            expandCodeBlock(filename.trim(), root.resolve(filename.trim()).readLines().withoutPreamble())
        } ?: error("No filename found for kotlin block")
    }

fun expandCodeBlock(filename: String, content: List<String>): String = (
    listOf(
        "[start-insert]: <$filename>",
        "```kotlin"
    ) +
        content +
        "```" +
        """<small>[$filename]($filename)</small>""" +
        "" +
        "[end-insert]: <>"

    ).joinToString("\n")

//language=RegExp
val expandedCodeBlockFinder = """^\[start-insert\]: <(.*?)>(.*?)^\[end-insert]:.*?$""".toRegex(setOf(DOT_MATCHES_ALL, MULTILINE))
