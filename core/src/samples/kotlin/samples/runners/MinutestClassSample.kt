@file:JvmName("MinutestSample")
package samples.runners

import dev.minutest.rootContext
import dev.minutest.test
import org.junit.platform.commons.annotation.Testable
import kotlin.test.fail


class MinutestClassSample {
    @Testable
    fun tests() = rootContext {
        runnersExample()
    }

    @Testable
    private fun `not tests as private`() = rootContext {
        test("SHOULD NOT BE SEEN") {
            fail("test from private fun discovered")
        }
    }

    @Testable
    @Suppress("UNUSED_PARAMETER")
    fun `not tests as have a parameter`(thing: Int) = rootContext {
        test("SHOULD NOT BE SEEN") {
            fail("test from fun with parameter discovered")
        }
    }
}