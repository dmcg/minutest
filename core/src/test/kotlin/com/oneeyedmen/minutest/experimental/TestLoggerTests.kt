package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.internal.RootExecutor
import org.junit.jupiter.api.Test
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException


class TestLoggerTests {

    val log = mutableListOf<String>()

    @Test fun test() {
        doStuff(TestLogger(log))
        assertLogged(log.withTabsExpanded(2),
            "▾ root",
            "  ✓ test in root",
            "  ✓ test 2 in root",
            "  ▾ outer",
            "    ✓ test in outer",
            "    ▾ inner",
            "      ✓ test in inner",
            "  ✓ test 3 in root",
            "  - skipped test in root",
            "  - aborted test in root",
            "  X failed test in root"
        )
    }

    @Test fun testPlain() {
        doStuff(TestLogger(log, TestLogger.noSymbols))
        assertLogged(log.withTabsExpanded(2),
            "root",
            "  test in root",
            "  test 2 in root",
            "  outer",
            "    test in outer",
            "    inner",
            "      test in inner",
            "  test 3 in root",
            "  skipped test in root",
            "  aborted test in root",
            "  failed test in root"
        )
    }

    private fun doStuff(logger: TestLogger) {
        logger.testComplete(Unit, RootExecutor.then("root").then("test in root"))
        logger.testComplete(Unit, RootExecutor.then("root").then("test 2 in root"))
        logger.testComplete(Unit, RootExecutor.then("root").then("outer").then("test in outer"))
        logger.testComplete(Unit, RootExecutor.then("root").then("outer").then("inner").then("test in inner"))
        logger.testComplete(Unit, RootExecutor.then("root").then("test 3 in root"))
        logger.testSkipped(Unit, RootExecutor.then("root").then("skipped test in root"), IncompleteExecutionException())
        logger.testAborted(Unit, RootExecutor.then("root").then("aborted test in root"), TestAbortedException())
        logger.testFailed(Unit, RootExecutor.then("root").then("failed test in root"), RuntimeException())
    }
}

private fun TestDescriptor.then(name: String): TestDescriptor = object : TestDescriptor {
    override val name: String = name
    override val parent = this@then
}
