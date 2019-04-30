package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException


class TestEventsTests {

    val log = mutableListOf<String>()

    val listener = object : TestEventListener {

        override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Starting " + testDescriptor.fullName())
        }

        override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Completed " + testDescriptor.fullName())
        }

        override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
            log.add("Failed " + testDescriptor.fullName())
        }

        override fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {
            log.add("Aborted " + testDescriptor.fullName())
        }

        override fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {
            log.add("Skipped " + testDescriptor.fullName())
        }

        override fun <PF, F> contextClosed(context: Context<PF, F>) {
            log.add("Closed " + context.name)
        }
    }

    @Test fun firesEvents() {

        val tests = rootContext<Unit> {
            addTransform { node ->
                node.telling(listener)
            }
            test("in root") {}
            context("outer") {
                test("in outer") {}
                context("inner") {
                    test("in inner") {}
                    test("fails") {
                        fail("Deliberate")
                    }
                    test("skipped with JUnit") {
                        throw TestSkippedException()
                    }
                    test("skipped with Minutest") {
                        throw MinutestSkippedException()
                    }
                    test("aborted") {
                        Assumptions.assumeFalse(true)
                    }
                    afterAll {
                        log.add("afterAll inner")
                    }
                }
                afterAll {
                    log.add("afterAll outer")
                }
            }
            afterAll {
                log.add("afterAll root")
            }
        }
        executeTests(tests)
        assertLogged(log,
            "Starting [root, in root]",
            "Completed [root, in root]",
            "Starting [root, outer, in outer]",
            "Completed [root, outer, in outer]",
            "Starting [root, outer, inner, in inner]",
            "Completed [root, outer, inner, in inner]",
            "Starting [root, outer, inner, fails]",
            "Failed [root, outer, inner, fails]",
            "Starting [root, outer, inner, skipped with JUnit]",
            "Skipped [root, outer, inner, skipped with JUnit]",
            "Starting [root, outer, inner, skipped with Minutest]",
            "Skipped [root, outer, inner, skipped with Minutest]",
            "Starting [root, outer, inner, aborted]",
            "Aborted [root, outer, inner, aborted]",
            "afterAll inner",
            "Closed inner",
            "afterAll outer",
            "Closed outer",
            "afterAll root",
            "Closed root"
        )
    }
}