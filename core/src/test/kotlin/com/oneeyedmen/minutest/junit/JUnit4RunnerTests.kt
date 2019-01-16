package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.rootContext
import com.oneeyedmen.minutest.runTestsInClass
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4RunnersThing
import samples.runners.expectedRunnersLog


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

fun checkRunnersExampleLog(log: List<String>,
    engineName: String,
    testName: String,
    rootName: String,
    noRegistration: Boolean = false
) = assertLogged(log, *expectedRunnersLog(engineName, testName, rootName, noRegistration).toTypedArray())
