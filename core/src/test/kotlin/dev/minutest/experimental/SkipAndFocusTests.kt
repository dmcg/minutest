package dev.minutest.experimental

import dev.minutest.RootContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import kotlin.test.fail


class SkipAndFocusTests : JUnit5Minutests {

    private val noop: Unit.(Unit) -> Unit = {}

    fun noop() = rootContext {
        test2("t1", noop)
        test2("t2", noop)
        willRun(
            "▾ noop",
            "  ✓ t1",
            "  ✓ t2"
        )
    }

    fun `skip test`() = rootContext {
        SKIP - test2("t1") { fail("t1 wasn't skipped") }
        test2("t2", noop)
        willRun(
            "▾ skip test",
            "  - t1",
            "  ✓ t2"
        )
    }

    fun `skip context`() = rootContext {
        SKIP - context("c1") {
            test2("c1/t1") { fail("c1/t1 wasn't skipped") }
        }
        test2("t2", noop)
        willRun(
            "▾ skip context",
            "  - c1",
            "  ✓ t2"
        )
    }

    fun `focus test skips unfocused`() = rootContext {
        test2("t1") { fail("t1 wasn't skipped") }
        FOCUS - test2("t2", noop)
        willRun(
            "▾ focus test skips unfocused",
            "  - t1",
            "  ✓ t2"
        )
    }

    fun `focus context skips unfocused`() = rootContext {
        test2("t1") { fail("t1 wasn't skipped") }
        FOCUS - context("c1") {
            test2("c1/t1", noop)
        }
        willRun(
            "▾ focus context skips unfocused",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1"
        )
    }

    fun `focus downtree skips unfocused from root`() = rootContext {
        test2("t1") { fail("t1 wasn't skipped") }
        context("c1") {
            FOCUS - test2("c1/t1", noop)
        }
        willRun(
            "▾ focus downtree skips unfocused from root",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1"
        )
    }

    fun `focus doesn't resurrect a skipped context`() = rootContext {
        SKIP - context("c1") {
            FOCUS - test2("c1/t1") {
                fail("was resurrected by focus despite skip")
            }
        }
        willRun(
            "▾ focus doesn't resurrect a skipped context",
            "  - c1"
        )
    }

    fun `deep thing`() = rootContext {
        test2("t1") { fail("t1 wasn't skipped") }
        context("c1") {
            FOCUS - test2("c1/t1", noop)
            context("c1/c1") {
                test2("c1/c1/t1") { fail("c1/c1/t1 wasn't skipped") }
            }
            FOCUS - context("c1/c2") {
                test2("c1/c2/t1", noop)
                SKIP - test2("c1/c2/t2") { fail("c1/c2/t2 wasn't skipped") }
            }
        }
        willRun(
            "▾ deep thing",
            "  - t1",
            "  ▾ c1",
            "    ✓ c1/t1",
            "    - c1/c1",
            "    ▾ c1/c2",
            "      ✓ c1/c2/t1",
            "      - c1/c2/t2"
        )
    }

    fun `skip from root`(): RootContextBuilder = SKIP - rootContext {
        test2("root was skipped") {
            fail("root wasn't skipped")
        }
        willRun(
            "- skip from root"
        )
    }
}
