@file:JvmName("MinutestSample")
package samples.runners

import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.platform.commons.annotation.Testable


class MinutestMultiRootClassSample {
    @Testable
    fun tests() = rootContext {
        test2("test in tests") {
        }
    }

    @Testable
    fun moreTests() = rootContext {
        test2("test in moreTests") {
        }
    }
}