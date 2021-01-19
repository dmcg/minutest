package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.ContextBuilder
import dev.minutest.TestDescriptor
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.random.Random


fun <F> ContextBuilder<F>.randomTest(
    name: String,
    block: F.(rng: Random, testDescriptor: TestDescriptor) -> F
): Annotatable<F> =
    test(name) { testDescriptor ->
        val seedFile = testDescriptor.testStateFile("random-seed")
        
        val seed = seedFile.maybeReadInt()
            ?: Random.nextInt().also {
                seedFile.parentFile.mkdirs()
                seedFile.writeText(it.toString())
            }
        
        block(Random(seed), testDescriptor)
            .also { seedFile.delete() }
    }


private val rootTestStateDir = Paths.get("build", "minutest")

private fun TestDescriptor.testStateFile(filename: String): File =
    fullName().fold(rootTestStateDir, Path::resolve).resolve(filename).toFile()

private fun File.maybeReadInt(): Int? {
    return try {
        takeIf { it.exists() }?.readText()?.toIntOrNull()
    }
    catch (e: IOException) {
        null
    }
}
