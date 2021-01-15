package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.fail
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException
import org.junit.jupiter.api.Test as JUnitTest


class TestEventsTests {

    val log = mutableListOf<String>()

    val listener = object : TestEventListener {

        override fun <PF, F> contextOpened(context: Context<PF, F>, testDescriptor: TestDescriptor) {
            log.add("Opened " + testDescriptor.fullName())
        }

        override fun <F> testStarting(test: Test<F>, fixture: F, testDescriptor: TestDescriptor) {
            log.add("Starting " + testDescriptor.fullName())
        }

        override fun <F> testComplete(test: Test<F>, fixture: F, testDescriptor: TestDescriptor) {
            log.add("Completed " + testDescriptor.fullName())
        }

        override fun <F> testFailed(test: Test<F>, fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
            log.add("Failed " + testDescriptor.fullName())
        }

        override fun <F> testAborted(test: Test<F>, fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {
            log.add("Aborted " + testDescriptor.fullName())
        }

        override fun <F> testSkipped(test: Test<F>, fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {
            log.add("Skipped " + testDescriptor.fullName())
        }

        override fun <PF, F> contextClosed(context: Context<PF, F>, testDescriptor: TestDescriptor) {
            log.add("Closed " + context.name)
        }
    }

    @JUnitTest fun firesEvents() {
        val tests = rootContext {
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
            "Opened [root]",
            "Starting [root, in root]",
            "Completed [root, in root]",
            "Opened [root, outer]",
            "Starting [root, outer, in outer]",
            "Completed [root, outer, in outer]",
            "Opened [root, outer, inner]",
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

    @JUnitTest fun `fires events with no test in root`() {
        val tests = rootContext {
            addTransform { node ->
                node.telling(listener)
            }
            context("context") {
                test("in context") {}
                afterAll {
                    log.add("afterAll context")
                }
            }
            afterAll {
                log.add("afterAll root")
            }
        }
        executeTests(tests)
        assertLogged(log,
            "Opened [root]",
            "Opened [root, context]",
            "Starting [root, context, in context]",
            "Completed [root, context, in context]",
            "afterAll context",
            "Closed context",
            "afterAll root",
            "Closed root"
        )
    }

    @JUnitTest fun `no events unless there are tests`() {
        val tests = rootContext {
            addTransform { node ->
                node.telling(listener)
            }
            context("context") {
            }
            afterAll {
                log.add("afterAll root")
            }
        }
        executeTests(tests)
        assertLogged(log)
    }
}