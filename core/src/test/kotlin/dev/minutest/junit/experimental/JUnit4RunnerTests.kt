package dev.minutest.junit.experimental

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.rootContext
import dev.minutest.runTestsInClass
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4RunnersThing


class JUnit4RunnerTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {
        test("JUnit 4 runs tests") {
            checkRunnersExampleLog(runTestsInClass<JUnit4RunnersThing>(
                VintageTestDescriptor.ENGINE_ID),
                "JUnit Vintage",
                "JUnit4RunnersThing",
                "root")
        }
    }
}
