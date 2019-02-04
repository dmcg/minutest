package com.oneeyedmen.minutest.examples.experimental

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.experimental.applyRule
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder


class JunitRulesExampleTests : JUnit5Minutests {

    class Fixture {
        // make rules part of the fixture, no need for an annotation
        val testFolder = TemporaryFolder()
    }

    override val tests = rootContext<Fixture> {

        fixture { Fixture() }

        // tell the context to use the rule for each test in it and its children
        applyRule { this.testFolder }

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
