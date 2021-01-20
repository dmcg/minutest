package dev.minutest.junit

import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test
import samples.runners.JUnit5Sample


class JUnit5RunnerTests {

    @Test fun test() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit5Sample>("junit-jupiter"),
            "JUnit Jupiter",
            "JUnit5Sample",
            "minutests()",
            abortRatherThanSkip = true
        )
    }
}


