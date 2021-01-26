package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test

class JUnit5MultiRootSample : JUnit5Minutests {

    fun tests() = rootContext {
        test("test in tests") {
        }
    }

    fun moreTests() = rootContext {
        test("test in moreTests") {
        }
    }
}

