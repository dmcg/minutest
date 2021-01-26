package dev.minutest.experimental

import dev.minutest.NodeId
import dev.minutest.Test
import dev.minutest.assertLogged
import dev.minutest.internal.AmalgamatedRootContext
import dev.minutest.internal.RootExecutor
import dev.minutest.internal.andThenName
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.junit.jupiter.api.Test as JUnitTest


class TestLoggerTests {

    @JUnitTest fun test() {
        val logger = TestLogger()
        doStuff(logger)
        assertLogged(logger.toStrings(),
            "▾ root",
            "✓ root/test in root",
            "✓ root/test 2 in root",
            "▾ root/outer",
            "✓ root/outer/test in outer",
            "▾ root/outer/inner",
            "✓ root/outer/inner/test in inner",
            "✓ root/test 3 in root",
            "- root/skipped test in root",
            "- root/aborted test in root",
            "X root/failed test in root",
        )
    }

    @JUnitTest fun testPlain() {
        val logger = TestLogger(prefixer = TestLogger.noSymbols)
        doStuff(logger)
        assertLogged(logger.toStrings(),
           "root",
           "root/test in root",
           "root/test 2 in root",
           "root/outer",
           "root/outer/test in outer",
           "root/outer/inner",
           "root/outer/inner/test in inner",
           "root/test 3 in root",
           "root/skipped test in root",
           "root/aborted test in root",
           "root/failed test in root",
        )
    }

    private fun doStuff(logger: TestLogger) {
        val stubContext = AmalgamatedRootContext("meh") { emptyList() }
        val stubTest = Test<Unit>("dummy", emptyList(), NodeId.forBuilder(this)) { _, _ -> }

        logger.contextOpened(stubContext, RootExecutor.andThenName("root"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenName("root").andThenName("test in root"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenName("root").andThenName("test 2 in root"))
        logger.contextOpened(stubContext, RootExecutor.andThenName("root").andThenName("outer"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenName("root").andThenName("outer").andThenName("test in outer"))
        logger.contextOpened(stubContext, RootExecutor.andThenName("root").andThenName("outer").andThenName("inner"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenName("root").andThenName("outer").andThenName("inner").andThenName("test in inner"))
        logger.contextClosed(stubContext, RootExecutor.andThenName("root").andThenName("outer").andThenName("inner"))
        logger.contextClosed(stubContext, RootExecutor.andThenName("root").andThenName("outer"))
        logger.testComplete(stubTest, Unit, RootExecutor.andThenName("root").andThenName("test 3 in root"))
        logger.testSkipped(stubTest, Unit, RootExecutor.andThenName("root").andThenName("skipped test in root"), IncompleteExecutionException())
        logger.testAborted(stubTest, Unit, RootExecutor.andThenName("root").andThenName("aborted test in root"), TestAbortedException())
        logger.testFailed(stubTest, Unit, RootExecutor.andThenName("root").andThenName("failed test in root"), RuntimeException())
        logger.contextClosed(stubContext, RootExecutor.andThenName("root"))
    }

}