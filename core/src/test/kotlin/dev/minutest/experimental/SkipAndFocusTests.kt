package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Test
import kotlin.test.fail


class SkipAndFocusTests {

    private val log = mutableListOf<String>()
    private val noop: Unit.(TestDescriptor) -> Unit = {}

    @Test fun noop() {
        val tests = rootContext {
            logTo(log)
            test("t1", noop)
            test("t2", noop)
        }
        checkLog(tests,
            "▾ root",
            "  ✓ t1",
            "  ✓ t2"
        )
    }

    @Test fun `skip test`() {
        val tests = rootContext {
            logTo(log)
            SKIP - test("t1") { fail("t1 wasn't skipped") }
            test("t2", noop)
        }
        checkLog(tests,
            "▾ root",
            "  - t1",
            "  ✓ t2"
        )
    }

    @Test fun `skip context`() {
        val tests = rootContext {
            logTo(log)
            SKIP - context("c1") {
                test("c1/t1") { fail("c1/t1 wasn't skipped") }
            }
            test("t2", noop)
        }
        checkLog(tests,
            "▾ root",
            "  - c1",
            "  ✓ t2"
        )
    }

    @Test fun `focus test skips unfocused`() {
        val tests = rootContext {
            logTo(log)
            test("t1") { fail("t1 wasn't skipped") }
            FOCUS - test("t2", noop)
        }
        checkLog(tests,
            "▾ root",
            "  - t1",
            "  ✓ t2"
        )
    }

    @Test fun `focus context skips unfocused`() {
        val tests = rootContext {
            logTo(log)
            test("t1") { fail("t1 wasn't skipped") }
            FOCUS - context("c1") {
                test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "▾ root",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1"
        )
    }

    @Test fun `focus downtree skips unfocused from root`() {
        val tests = rootContext {
            logTo(log)
            test("t1") { fail("t1 wasn't skipped") }
            context("c1") {
                FOCUS - test("c1/t1", noop)
            }
        }
        checkLog(tests,
            "▾ root",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1"
        )
    }

    @Test fun `focus doesn't resurrect a skipped context`() {
        val tests = rootContext {
            logTo(log)
            SKIP - context("c1") {
                FOCUS - test("c1/t1") {
                    fail("was resurrected by focus despite skip")
                }
            }
        }
        checkLog(tests,
            "▾ root",
            "  - c1"
        )
    }

    @Test fun `deep thing`() {
        val tests = rootContext {
            logTo(log)
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
            "▾ root",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1",
            "    - c1/c1",
            "    ▾ c1/c2",
            "      ✓ c1/c2/t1",
            "      - c1/c2/t2"
        )
    }

    @Test fun `skip from root`() {
        val tests = SKIP - rootContext {
            logTo(log)
            test("root was skipped") {
                fail("root wasn't skipped")
            }
        }
        checkLog(tests,
            "- root"
        )
    }

    private fun checkLog(tests: RootContextBuilder, vararg expected: String) {
        executeTests(tests)
        assertLogged(log, *expected)
    }
}
