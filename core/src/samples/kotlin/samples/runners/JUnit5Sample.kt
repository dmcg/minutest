package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

class JUnit5Sample : JUnit5Minutests {

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

