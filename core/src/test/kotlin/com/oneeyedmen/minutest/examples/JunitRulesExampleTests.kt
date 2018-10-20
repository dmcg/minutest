package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.examples.JunitRulesExampleTests.Fixture
import com.oneeyedmen.minutest.junit.JUnitFixtureTests
import com.oneeyedmen.minutest.junit.applyRule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder


object JunitRulesExampleTests : JUnitFixtureTests<Fixture>() {

    class Fixture {
        // make rules part of the fixture, no need for an annotation
        val testFolder = TemporaryFolder()
    }

    override val tests = tests {

        fixture { Fixture() }

        // tell the context to use the rule for each test in it and its children
        applyRule { this.testFolder }

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
