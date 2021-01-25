package samples.runners

import dev.minutest.junit.experimental.JUnit4Minutests
import dev.minutest.rootContext
import dev.minutest.test2

class JUnit4MultiRootSample : JUnit4Minutests() {

    fun tests() = rootContext {
        test2("test in tests") {
        }
    }

    fun moreTests() = rootContext {
        test2("test in moreTests") {
        }
    }
}

