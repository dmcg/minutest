package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


class TestEventsTests {

    val log = mutableListOf<String>()

    val listener = object : TestEventListener {

        override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Completed " + testDescriptor.fullName())
        }

        override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
            log.add("Failed " + testDescriptor.fullName())
        }

        override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Starting " + testDescriptor.fullName())
        }

        override fun <PF, F> contextClosed(runtimeContext: RuntimeContext<PF, F>) {
            log.add("Closed " + runtimeContext.name)
        }
    }

    @Test fun firesEvents() {

        val tests = rootContext<Unit> {
            annotateWith(Telling(listener))
            test("in root") {}
            context("outer") {
                test("in outer") {}
                context("inner") {
                    test("in inner") {}
                    test("fails") {
                        fail("Deliberate")
                    }
                }
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
            "Closed inner",
            "Closed outer",
            "Closed root"
        )
    }
}