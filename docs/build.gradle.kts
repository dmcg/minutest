import java.io.File

tasks {
    val readme = create("readme") {
        doFirst {
            processMarkDown(project.projectDir.resolve("src"))
        }
    }

   "build" {
        dependsOn(readme)
    }
}

fun processMarkDown(dir: File) {
    dir.listFiles().filter { it.name.endsWith("template.md") }.forEach { file ->
        processMarkDown(file, file.parentFile.resolve("../" + file.name.replace(".template", "")))
    }
}

fun processMarkDown(src: File, dest: File) {
    val newReadmeLines = src.readLines().map { line ->
        "```insert-kotlin (.*)$".toRegex().find(line)?.groups?.get(1)?.value?.let { filename ->
            (listOf("```kotlin") + linesFrom(filename).filtered()).joinToString("\n")
        } ?: line
    }
    dest.writeText(newReadmeLines.joinToString("\n"))
}

fun Iterable<String>.filtered(): List<String> = this
    .filter { ! it.startsWith("import") && ! it.startsWith("package") }
    .dropWhile { it.isEmpty() }

fun linesFrom(filename: String) = File(filename).readLines()
