package dev.minutest.junit.experimental

import dev.minutest.experimental.willRun
import dev.minutest.given
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertEquals
import org.opentest4j.TestAbortedException

class JUnit4MinutestsTests : JUnit4Minutests() {

    fun `my tests`() = rootContext<String> {

        given { "banana" }

        test2("test") {
            assertEquals("banana", it)
        }

        context("context") {
            test2("test x") {}
            context("another context") {
                test2("test y") {}
            }
            context("empty context") {
            }
            context("context whose name is wrong if you just run this test in IntelliJ") {
                // TODO 2018-12-08 DMCG - fix nested context name in IntelliJ
                test2("test") {}
            }
            test2("skipped") {
                throw TestAbortedException("should be skipped")
            }
        }

        willRun(
            "▾ my tests",
            "  ✓ test",
            "  ▾ context",
            "    ✓ test x",
            "    ▾ another context",
            "      ✓ test y",
            "  ▾ context whose name is wrong if you just run this test in IntelliJ",
            "    ✓ test",
            "  - skipped"
        )
    }
}