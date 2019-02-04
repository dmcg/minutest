package uk.org.minutest.junit

import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4RunnersThing
import samples.runners.expectedRunnersLog
import uk.org.minutest.assertLogged
import uk.org.minutest.rootContext
import uk.org.minutest.runTestsInClass


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
