package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2

class JUnit5MultiRootSample : JUnit5Minutests {

    fun tests() = rootContext {
        test2("test in tests") {
        }
    }

    fun moreTests() = rootContext {
        test2("test in moreTests") {
        }
    }
}

