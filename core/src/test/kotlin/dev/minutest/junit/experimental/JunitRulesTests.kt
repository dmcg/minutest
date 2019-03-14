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

        applyRule { fixture.rule }

        test("test in root") {
            log.add(it.name)
        }

        context("context") {
            test("test in context") {
                log.add(it.name)
            }
        }

        derivedContext<Unit>("unit context") {
            deriveFixture { Unit }
            test("test in unit context") {
                log.add(it.name)
            }
        }

        derivedContext<String?>("nullable context") {
            deriveFixture { null }
            context("null context") {
                test("test in null context") {
                    log.add(it.name)
                }
            }
            context("not null context") {
                deriveFixture { "banana" }
                test_("test in not null context") {
                    log.add(it.name)
                    "kumquat"
                }
                after {
                    assertEquals("kumquat", fixture)
                }
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
                    "context.test in context(dev.minutest.junit.experimental.JunitRulesTests)",
                    "test in unit context",
                    "unit context.test in unit context(dev.minutest.junit.experimental.JunitRulesTests)",
                    "test in null context",
                    "nullable context.null context.test in null context(dev.minutest.junit.experimental.JunitRulesTests)",
                    "test in not null context",
                    "nullable context.not null context.test in not null context(dev.minutest.junit.experimental.JunitRulesTests)"
                ),
                log)
        }
    }
}



