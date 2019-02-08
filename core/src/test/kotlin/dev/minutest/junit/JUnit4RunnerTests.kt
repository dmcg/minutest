package dev.minutest.junit

import dev.minutest.assertLogged
import dev.minutest.rootContext
import dev.minutest.runTestsInClass
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4RunnersThing
import samples.runners.expectedRunnersLog


class JUnit4RunnerTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        test("JUnit 4 runs tests") {
            checkRunnersExampleLog(runTestsInClass<JUnit4RunnersThing>(VintageTestDescriptor.ENGINE_ID),
                "JUnit Vintage",
                "JUnit4RunnersThing",
                "root")
        }
    }
}

fun checkRunnersExampleLog(log: List<String>, engineName: String, testName: String, rootName: String) =
    assertLogged(log, *expectedRunnersLog(engineName, testName, rootName).toTypedArray())
