import java.io.File

tasks {
    val readme = create("readme") {
        doFirst {
            generateReadme()
        }
    }

   "build" {
        dependsOn(readme)
    }
}

fun generateReadme() {
    val newReadmeLines = linesFrom("README.template.md").map { line ->
        "```insert-kotlin (.*)$".toRegex().find(line)?.groups?.get(1)?.value?.let { filename ->
            (listOf("```kotlin") + linesFrom(filename).filtered()).joinToString("\n")
        } ?: line
    }
    File("README.md").writeText(newReadmeLines.joinToString("\n"))
}

fun Iterable<String>.filtered(): List<String> = this
    .filter { ! it.startsWith("import") && ! it.startsWith("package") }
    .dropWhile { it.isEmpty() }

fun linesFrom(filename: String) = File(filename).readLines()
