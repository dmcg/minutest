package dev.minutest.junit.experimental

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.rules.TestWatcher
import org.junit.runner.Description

private val log = mutableListOf<String>()

class JunitRulesTests : JUnit5Minutests {
    class TestRule : TestWatcher() {
        var testDescription: String? = null
        
        override fun succeeded(description: Description) {
            testDescription = description.displayName
        }
    }
    
    class Fixture {
        val rule = TestRule()
    }

    fun tests() = rootContext<Fixture>(name = javaClass.canonicalName) {
        fixture {
            Fixture()
        }

        applyRule { this.rule }

        test("test in root") {
            log.add(it.name)
        }

        context("context") {
            test("test in context") {
                log.add(it.name)
            }
        }

        after {
            log.add(rule.testDescription.toString())
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun checkTestIsRun() {
            assertEquals(
                listOf(
                    "test in root",
                    "test in root(dev.minutest.junit.experimental.JunitRulesTests)",
                    "test in context",
                    "context.test in context(dev.minutest.junit.experimental.JunitRulesTests)"),
                log)
        }
    }
}



