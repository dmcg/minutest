package dev.minutest.junit.experimental

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.runTestsInClass
import org.junit.jupiter.api.Test
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4Sample


class JUnit4RunnerTests {

    @Test fun test() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit4Sample>(VintageTestDescriptor.ENGINE_ID),
            "JUnit Vintage",
            "JUnit4Sample",
            "root",
            abortRatherThanSkip = false
        )
    }
}
