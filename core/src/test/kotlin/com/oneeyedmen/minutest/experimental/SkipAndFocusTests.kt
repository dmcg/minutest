package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.executeTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Test
import kotlin.test.fail


class SkipAndFocusTests {

    private val log = mutableListOf<String>()
    private val noop: Unit.() -> Unit = {}

    @Test fun noop() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {

            test("t1", noop)
            test("t2", noop)
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    t2"
        )
    }

    @Test fun `skip test`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            SKIP - test("t1", noop)
            test("t2", noop)
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    t2"
        )
    }

    @Test fun `skip context`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            SKIP - context("c1") {
                test("c1/t1", noop)
            }
            test("t2", noop)
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    c1",
            "    t2"
        )
    }

    @Test fun `focus test skips unfocused`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            test("t1", noop)
            FOCUS - test("t2", noop)
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    t2"
        )
    }

    @Test fun `focus context skips unfocused`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            test("t1", noop)
            FOCUS - context("c1") {
                test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    c1",
            "        c1/t1"
        )
    }

    @Test fun `focus downtree skips unfocused from root`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            test("t1", noop)
            context("c1") {
                FOCUS - test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    c1",
            "        c1/t1"
        )
    }

    @Test fun `deep thing`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            test("t1", noop)
            context("c1") {
                FOCUS - test("c1/t1", noop)
                context("c1/c1") {
                    test("c1/c1/t1", noop)
                }
                FOCUS - context("c1/c2") {
                    test("c1/c2/t1", noop)
                    SKIP - test("c1/c2/t2", noop)
                }
            }
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests",
            "    t1",
            "    c1",
            "        c1/t1",
            "        c1/c1",
            "        c1/c2",
            "            c1/c2/t1",
            "            c1/c2/t2"
        )
    }

    @Test fun `skip from root`() {
        val tests = context<Unit>(skipAndFocus.then(loggedTo(log))) {
            annotateWith(SKIP)
            test("root was skipped") {
                fail("root wasn't skipped")
            }
        }
        checkLog(tests,
            "com.oneeyedmen.minutest.experimental.SkipAndFocusTests"
        )
    }

    private fun checkLog(tests: NodeBuilder<Unit>, vararg expected: String) {
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4), *expected)
    }
}
