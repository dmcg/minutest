package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.executeTests
import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Test
import kotlin.test.fail


class SkipAndFocusTests {

    private val log = mutableListOf<String>()
    private val noop: Unit.() -> Unit = {}

    @Test fun noop() {
        val tests = rootContext<Unit>(loggedTo(log)) {

            test("t1", noop)
            test("t2", noop)
        }
        checkLog(tests,
            "root",
            "    t1",
            "    t2"
        )
    }

    @Test fun `skip test`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            SKIP - test("t1") { fail("t1 wasn't skipped") }
            test("t2", noop)
        }
        checkLog(tests,
            "root",
            "    t1",
            "    t2"
        )
    }

    @Test fun `skip context`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            SKIP - context("c1") {
                test("c1/t1") { fail("c1/t1 wasn't skipped") }
            }
            test("t2", noop)
        }
        checkLog(tests,
            "root",
            "    c1",
            "        skipping c1",
            "    t2"
        )
    }

    @Test fun `focus test skips unfocused`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            test("t1") { fail("t1 wasn't skipped") }
            FOCUS - test("t2", noop)
        }
        checkLog(tests,
            "root",
            "    t1",
            "    t2"
        )
    }

    @Test fun `focus context skips unfocused`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            test("t1") { fail("t1 wasn't skipped") }
            FOCUS - context("c1") {
                test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "root",
            "    t1",
            "    c1",
            "        c1/t1"
        )
    }

    @Test fun `focus downtree skips unfocused from root`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            test("t1") { fail("t1 wasn't skipped") }
            context("c1") {
                FOCUS - test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "root",
            "    t1",
            "    c1",
            "        c1/t1"
        )
    }

    @Test fun `deep thing`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            test("t1") { fail("t1 wasn't skipped") }
            context("c1") {
                FOCUS - test("c1/t1", noop)
                context("c1/c1") {
                    test("c1/c1/t1") { fail("c1/c1/t1 wasn't skipped") }
                }
                FOCUS - context("c1/c2") {
                    test("c1/c2/t1", noop)
                    SKIP - test("c1/c2/t2") { fail("c1/c2/t2 wasn't skipped") }
                }
            }
        }
        checkLog(tests,
            "root",
            "    t1",
            "    c1",
            "        c1/t1",
            "        c1/c1",
            "            skipping c1/c1",
            "        c1/c2",
            "            c1/c2/t1",
            "            c1/c2/t2"
        )
    }

    @Test fun `skip from root`() {
        val tests = rootContext<Unit>(loggedTo(log)) {
            annotateWith(SKIP)
            test("root was skipped") {
                fail("root wasn't skipped")
            }
        }
        checkLog(tests,
            "root",
            "    skipping root"
        )
    }

    private fun checkLog(tests: TopLevelContextBuilder<*>, vararg expected: String) {
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4), *expected)
    }
}
