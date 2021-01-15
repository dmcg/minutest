package dev.minutest.experimental

import dev.minutest.Test
import dev.minutest.assertLogged
import dev.minutest.internal.AmalgamatedRootContext
import dev.minutest.internal.RootExecutor
import dev.minutest.internal.andThenTestName
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.junit.jupiter.api.Test as JUnitTest


class TestLoggerTests {

    @JUnitTest fun test() {
        val logger = TestLogger()
        doStuff(logger)
        assertLogged(logger.toStrings(),
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

    @JUnitTest fun testPlain() {
        val logger = TestLogger(indent = "..", prefixer = TestLogger.noSymbols)
        doStuff(logger)
        assertLogged(logger.toStrings(),
            "root",
            "..test in root",
            "..test 2 in root",
            "..outer",
            "....test in outer",
            "....inner",
            "......test in inner",
            "..test 3 in root",
            "..skipped test in root",
            "..aborted test in root",
            "..failed test in root"
        )
    }

    private fun doStuff(logger: TestLogger) {
        val stubContext = AmalgamatedRootContext("meh", emptyList())
        val stubTest = Test<Unit>("dummy", emptyList(), { f, _  -> f})

        logger.contextOpened(stubContext, RootExecutor.andThenTestName("root"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("test in root"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("test 2 in root"))
        logger.contextOpened(stubContext, RootExecutor.andThenTestName("root").andThenTestName("outer"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("outer").andThenTestName("test in outer"))
        logger.contextOpened(stubContext, RootExecutor.andThenTestName("root").andThenTestName("outer").andThenTestName("inner"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("outer").andThenTestName("inner").andThenTestName("test in inner"))
        logger.contextClosed(stubContext, RootExecutor.andThenTestName("root").andThenTestName("outer").andThenTestName("inner"))
        logger.contextClosed(stubContext, RootExecutor.andThenTestName("root").andThenTestName("outer"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("test 3 in root"))
        logger.testSkipped(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("skipped test in root"), IncompleteExecutionException())
        logger.testAborted(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("aborted test in root"), TestAbortedException())
        logger.testFailed(stubTest, Unit, RootExecutor.andThenTestName("root").andThenTestName("failed test in root"), RuntimeException())
        logger.contextClosed(stubContext, RootExecutor.andThenTestName("root"))
    }

}