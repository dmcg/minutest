package com.oneeyedmen.minutest.junit

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestFactory
import org.junit.rules.TestWatcher
import org.junit.runner.Description


class JunitRulesTests {

    private val log = mutableListOf<String>()

    class Fixture {
        val rule = TestRule()
    }

    @TestFactory fun test() = junitTests<Fixture> {
        fixture {
            Fixture()
        }

        context("apply rule") {
            applyRule(Fixture::rule)

            test("test") {
                log.add("test")
            }
        }

        after {
            log.add(rule.testDescription.toString())
        }
    }

    @AfterEach fun checkTestIsRun() {
        assertEquals(listOf("test", "apply rule/test(Minutest)"), log)
    }
}

class TestRule : TestWatcher() {

    var testDescription: String? = null

    override fun succeeded(description: Description) {
        testDescription = description.displayName
    }
}


