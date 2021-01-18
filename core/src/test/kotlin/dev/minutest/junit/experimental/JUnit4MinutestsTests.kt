package dev.minutest.junit.experimental

import dev.minutest.assertLogged
import dev.minutest.experimental.logTo
import dev.minutest.rootContext
import org.junit.AfterClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.opentest4j.TestAbortedException

class JUnit4MinutestsTests
//    : JUnit4Minutests()
{

    fun `my tests`() = rootContext<String> {

        logTo(testLog)

        fixture { "banana" }

        test("test") {
            assertEquals("banana", fixture)
        }

        context("context") {
            test("test x") {}
            context("another context") {
                test("test y") {}
            }
            context("empty context") {
            }
            context("context whose name is wrong if you just run this test in IntelliJ") {
                // TODO 2018-12-08 DMCG - fix nested context name in IntelliJ
                test("test") {}
            }
            test("skipped") {
                throw TestAbortedException("should be skipped")
            }
        }
    }

    companion object {
        private val testLog = mutableListOf<String>()

        @AfterClass @JvmStatic fun check() {
            assertLogged(testLog,
                "▾ my tests",
                "✓ my tests/test",
                "▾ my tests/context",
                "✓ my tests/context/test x",
                "▾ my tests/context/another context",
                "✓ my tests/context/another context/test y",
                "▾ my tests/context/context whose name is wrong if you just run this test in IntelliJ",
                "✓ my tests/context/context whose name is wrong if you just run this test in IntelliJ/test",
                "- my tests/context/skipped"
            )
        }
    }
}