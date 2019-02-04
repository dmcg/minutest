package uk.org.minutest.examples.experimental

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.junit.experimental.applyRule
import uk.org.minutest.rootContext


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
