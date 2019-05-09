package samples.runners

import dev.minutest.junit.experimental.JUnit4Minutests
import dev.minutest.rootContext

class JUnit4Sample : JUnit4Minutests() {

    fun tests() = rootContext {
        runnersExample()
    }

    private fun `not tests as private`() = rootContext {
        runnersExample()
    }

    @Suppress("UNUSED_PARAMETER")
    fun `not tests as have a parameter`(thing: Int) = rootContext {
        runnersExample()
    }
}

