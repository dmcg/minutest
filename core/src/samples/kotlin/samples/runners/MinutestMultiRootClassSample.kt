@file:JvmName("MinutestSample")
package samples.runners

import dev.minutest.rootContext
import dev.minutest.test
import org.junit.platform.commons.annotation.Testable


class MinutestMultiRootClassSample {
    @Testable
    fun tests() = rootContext {
        test("test in tests") {
        }
    }

    @Testable
    fun moreTests() = rootContext {
        test("test in moreTests") {
        }
    }
}