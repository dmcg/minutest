package dev.minutest.junit.experimental

import dev.minutest.assertLogged
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.AfterAll
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class JUnitRulesTests : JUnit5Minutests {

    class TestRule : TestWatcher() {
        var testDescription: String? = null

        override fun succeeded(description: Description) {
            testDescription = description.displayName
        }
    }

    class Fixture {
        val rule = TestRule()
    }

    fun `non-null fixture`() = rootContext<Fixture>(name = "non-null fixture") {
        fixture {
            Fixture()
        }

        applyRule { fixture.rule }

        test("test in root") {}

        context("context") {
            test("test in context") {}
        }
        after {
            log.add(rule.testDescription.toString())
        }
    }

    // Show that we differentiate between a null fixture and a fixture that hasn't been set
    fun `null fixture`() = rootContext<Fixture?>(name = "null fixture") {
        fixture {
            null
        }

        applyRule {
            // We can't go to the fixture for the rule, but we can just return one
            staticRule
        }

        test("test in root") {}

        context("context") {
            test("test in context") {}
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun checkTestIsRun() {
            // The (root) is the name of the root context created to hold the two test method root contexts
            assertLogged(log,
                "non-null fixture.test in root(root)",
                "non-null fixture.context.test in context(root)",
                "null fixture.test in root(root)",
                "null fixture.context.test in context(root)"
            )
        }
    }
}

private val log = mutableListOf<String>()

private val staticRule = object : TestWatcher() {
    override fun succeeded(description: Description) {
        log.add(description.toString())
    }
}



