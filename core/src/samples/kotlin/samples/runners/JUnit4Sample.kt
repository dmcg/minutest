package samples.runners

import dev.minutest.junit.experimental.JUnit4Minutests
import dev.minutest.rootContext
import dev.minutest.test
import kotlin.test.fail

class JUnit4Sample : JUnit4Minutests() {

    fun tests() = rootContext {
        runnersExample()
    }

    private fun `not tests as private`() = rootContext {
        test("SHOULD NOT BE SEEN") {
            fail("test from private fun discovered")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun `not tests as have a parameter`(thing: Int) = rootContext {
        test("SHOULD NOT BE SEEN") {
            fail("test from fun with parameter discovered")
        }
    }
}

