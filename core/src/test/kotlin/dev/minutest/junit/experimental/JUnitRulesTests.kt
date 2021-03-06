package dev.minutest.junit.experimental

import dev.minutest.*
import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.AfterAll
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.*
import java.util.Collections.synchronizedList

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
        given {
            Fixture()
        }

        applyRule(this@JUnitRulesTests::class.java.simpleName) { fixture ->
            fixture.rule
        }

        test("test in root") {}

        context("context") {
            test("test in context") {}
        }
        afterEach {
            log.add(rule.testDescription.toString())
        }
    }

    // Show that we differentiate between a null fixture and a fixture that hasn't been set
    fun `null fixture`() = rootContext<Fixture?>(name = "null fixture") {
        given {
            null
        }

        applyRule(this@JUnitRulesTests::class.java.simpleName) {
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
            assertLoggedInAnyOrder(log,
                "non-null fixture.test in root(JUnitRulesTests)",
                "non-null fixture.context.test in context(JUnitRulesTests)",
                "null fixture.test in root(JUnitRulesTests)",
                "null fixture.context.test in context(JUnitRulesTests)"
            )
        }
    }
}

private val log: MutableList<String> = synchronizedList(mutableListOf<String>())

private val staticRule = object : TestWatcher() {
    override fun succeeded(description: Description) {
        log.add(description.toString())
    }
}



