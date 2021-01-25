package dev.minutest.junit

import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test
import samples.runners.JUnit5MultiRootSample
import samples.runners.JUnit5Sample
import samples.runners.expected
import samples.runners.multiRootExpected


class JUnit5RunnerTests {

    @Test fun singleRootTestsAndFailures() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit5Sample>("junit-jupiter"),
            substitutions = listOf(
                "ENGINE_NAME" to "JUnit Jupiter",
                "TEST_NAME" to "JUnit5Sample",
                "ROOT_NAME" to "minutests()"
            ),
            abortRatherThanSkip = true,
            expected = expected
        )
    }

    @Test fun multiRoot() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit5MultiRootSample>("junit-jupiter"),
            substitutions = listOf(
                "ENGINE_NAME" to "JUnit Jupiter",
                "TEST_NAME" to "JUnit5MultiRootSample",
                "ROOT_NAME" to "minutests()"
            ),
            abortRatherThanSkip = true,
            expected = multiRootExpected
        )
    }
}


