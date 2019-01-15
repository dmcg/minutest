package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.rootContext
import com.oneeyedmen.minutest.runTestsInClass
import example.runners.JUnit4RunnersThing
import example.runners.checkRunnersExampleLog
import org.junit.vintage.engine.descriptor.VintageTestDescriptor


class JUnit4RunnerTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        test("JUnit 4 runs tests") {
            checkRunnersExampleLog(runTestsInClass<JUnit4RunnersThing>(VintageTestDescriptor.ENGINE_ID),
                "JUnit Vintage",
                "JUnit4RunnersThing",
                "root",
                noRegistration = true)
        }
    }
}
