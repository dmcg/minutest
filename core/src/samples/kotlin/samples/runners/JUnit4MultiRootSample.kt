package samples.runners

import dev.minutest.junit.experimental.JUnit4Minutests
import dev.minutest.rootContext
import dev.minutest.test

class JUnit4MultiRootSample : JUnit4Minutests() {

    fun tests() = rootContext {
        test("test in tests") {
        }
    }

    fun moreTests() = rootContext {
        test("test in moreTests") {
        }
    }
}

