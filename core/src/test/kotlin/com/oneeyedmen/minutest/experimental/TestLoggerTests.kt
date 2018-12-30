package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.internal.RootExecutor
import org.junit.jupiter.api.Test


class TestLoggerTests {

    @Test fun test() {
        val log = mutableListOf<String>()
        val logger = TestLogger(log)
        logger.testComplete(Unit, RootExecutor.then("root").then("test in root"))
        logger.testComplete(Unit, RootExecutor.then("root").then("test 2 in root"))
        logger.testComplete(Unit, RootExecutor.then("root").then("outer").then("test in outer"))
        logger.testComplete(Unit, RootExecutor.then("root").then("outer").then("inner").then("test in inner"))
        logger.testComplete(Unit, RootExecutor.then("root").then("test 3 in root"))
        assertLogged(log.withTabsExpanded(2),
            "root",
            "  test in root",
            "  test 2 in root",
            "  outer",
            "    test in outer",
            "    inner",
            "      test in inner",
            "  test 3 in root"
        )
    }
}

private fun TestDescriptor.then(name: String): TestDescriptor = object : TestDescriptor {
    override val name: String = name
    override val parent = this@then
}
