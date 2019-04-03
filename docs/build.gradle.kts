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
    val enhancedLines = src.readLines().map { line ->
        "```insert-kotlin (.*)$".toRegex().find(line)?.groups?.get(1)?.value?.let { filename ->
            (listOf("```kotlin") + linesFrom(filename).filtered()).joinToString("\n")
        } ?: line
    }
    dest.writeText((headerFor(src) + enhancedLines).joinToString("\n"))
}

fun Iterable<String>.filtered(): List<String> = this
    .filter { ! it.startsWith("import") && ! it.startsWith("package") }
    .dropWhile { it.isEmpty() }

fun linesFrom(filename: String) = File(filename).readLines()

fun headerFor(file: File) = if (file.name.startsWith("README.")) emptyList() else listOf(header)

private val header = "[Minutest](README.md)\n"