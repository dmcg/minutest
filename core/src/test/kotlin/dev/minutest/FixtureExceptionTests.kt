package dev.minutest

import dev.minutest.testing.runTests
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException


class FixtureExceptionTests  {

    @Test
    fun `throws exception thrown from fixture during execution`() {
        val tests = rootContext<String> {
            given {
                throw FileNotFoundException()
            }
            test("there needs to be a test to run anything") {}

        }
        runTests(tests).hasExceptionsMatching(
            { it is FileNotFoundException }
        )
    }
}