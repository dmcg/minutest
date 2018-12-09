package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.junit.MinutestJUnit4Runner
import com.oneeyedmen.minutest.junit.context
import org.junit.Test
import org.junit.runner.RunWith
import org.opentest4j.TestAbortedException
import kotlin.test.assertEquals


@RunWith(MinutestJUnit4Runner::class)
class MinutestJUnit4RunnerTests : JUnit4Minutests {

    override val tests = context<Unit>(loggedTo(testLog)) {

        test("test") {}

        context("context") {
            test("test x") {}
            test("test 2") {
//                fail("here")
            }
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
}

private val testLog = mutableListOf<String>()

// This name seems to make JUnit run this after the above
class AMinutestJUnit4RunnerTestsVerifier {

    @Test fun `check the other run`() {
        assertEquals(listOf(
            "com.oneeyedmen.minutest.experimental.MinutestJUnit4RunnerTests",
            "    test",
            "    context",
            "        test x",
            "        test 2",
            "        another context",
            "            test y",
            "        empty context",
            "        context whose name is wrong if you just run this test in IntelliJ",
            "            test",
            "        skipped"),
            testLog.withTabsExpanded(4))

    }
}

