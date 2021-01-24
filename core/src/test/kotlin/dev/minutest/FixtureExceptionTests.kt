package dev.minutest

import dev.minutest.testing.runTests
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException


class FixtureExceptionTests  {

    @Test
    fun `throws exception thrown from fixture during execution`() {
        val tests = rootContext<String> {
            fixture {
                throw FileNotFoundException()
            }
            test2("there needs to be a test to run anything") {}

        }
        runTests(tests).hasExceptionsMatching(
            { it is FileNotFoundException }
        )
    }
}