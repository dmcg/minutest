package dev.minutest.examples

import dev.minutest.afterEach
import dev.minutest.closeableFixture
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CloseableFixtureExampleTests : JUnit5Minutests {

    class Fixture(file: File): Closeable {

        val writer = FileWriter(file)

        override fun close() = writer.close()
    }

    fun tests() = rootContext<Fixture> {

        closeableFixture { testDescriptor ->
            Fixture(File.createTempFile(testDescriptor.name, ".tmp"))
        }

        test2("can write") {
            writer.write("banana")
        }

        afterEach {
            assertThrows(IOException::class.java) {
                writer.write("should be closed")
            }
        }
    }
}