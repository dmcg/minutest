package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.applyRule
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import org.junit.rules.TemporaryFolder


object JunitRulesExampleTests {

    class Fixture {
        // make rules part of the fixture
        val testFolder = TemporaryFolder()
    }

    @TestFactory fun `temporary folder rule`() = junitTests<Fixture>() {

        fixture { Fixture() }

        // tell the context to use the rule
        applyRule(Fixture::testFolder)

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
