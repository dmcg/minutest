package dev.minutest

import org.junit.jupiter.api.Test
import java.io.FileNotFoundException


class FixtureExceptionTests  {

    @Test
    fun `throws exception thrown from fixture during execution`() {
        val tests = rootContext<String> {
            fixture {
                throw FileNotFoundException()
            }
            test("there needs to be a test to run anything") {}

        }
        checkItems(executeTests(tests), { it is FileNotFoundException })
    }
}