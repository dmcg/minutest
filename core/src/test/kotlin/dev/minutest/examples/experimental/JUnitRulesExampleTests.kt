package dev.minutest.examples.experimental

import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.experimental.applyRule
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder


class JUnitRulesExampleTests : JUnit5Minutests {

    class Fixture {
        // make rules part of the fixture, no need for an annotation
        val testFolder = TemporaryFolder()
    }

    fun tests() = rootContext<Fixture> {

        given { Fixture() }

        // tell the context to use the rule for each test in it and its children
        applyRule(this@JUnitRulesExampleTests::class.java.name) { this.testFolder }

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
