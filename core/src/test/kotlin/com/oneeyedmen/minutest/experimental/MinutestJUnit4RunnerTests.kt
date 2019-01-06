package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.AfterClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.opentest4j.TestAbortedException

class MinutestJUnit4RunnerTests : JUnit4Minutests() {

    fun tests() = rootContext<String>(loggedTo(testLog)) {

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
                "▾ root",
                "  ✓ test",
                "  ▾ context",
                "    ✓ test x",
                "    ▾ another context",
                "      ✓ test y",
                "    ▾ context whose name is wrong if you just run this test in IntelliJ",
                "      ✓ test",
                "    - skipped"
            )
        }
    }
}