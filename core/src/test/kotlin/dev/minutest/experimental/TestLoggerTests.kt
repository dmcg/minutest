package dev.minutest.experimental

import dev.minutest.assertLogged
import dev.minutest.internal.RootExecutor
import dev.minutest.internal.andThen
import org.junit.jupiter.api.Test
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException


class TestLoggerTests {

    val log = mutableListOf<String>()

    @Test fun test() {
        doStuff(TestLogger(log))
        assertLogged(log,
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
        doStuff(TestLogger(log, indent = "..", prefixer = TestLogger.noSymbols))
        assertLogged(log,
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
        logger.testComplete(Unit, RootExecutor.andThen("root").andThen("test in root"))
        logger.testComplete(Unit, RootExecutor.andThen("root").andThen("test 2 in root"))
        logger.testComplete(Unit, RootExecutor.andThen("root").andThen("outer").andThen("test in outer"))
        logger.testComplete(Unit, RootExecutor.andThen("root").andThen("outer").andThen("inner").andThen("test in inner"))
        logger.testComplete(Unit, RootExecutor.andThen("root").andThen("test 3 in root"))
        logger.testSkipped(Unit, RootExecutor.andThen("root").andThen("skipped test in root"), IncompleteExecutionException())
        logger.testAborted(Unit, RootExecutor.andThen("root").andThen("aborted test in root"), TestAbortedException())
        logger.testFailed(Unit, RootExecutor.andThen("root").andThen("failed test in root"), RuntimeException())
    }
}