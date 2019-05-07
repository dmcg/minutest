package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

class JUnit5Sample : JUnit5Minutests {

    fun tests() = rootContext {
        runnersExample()
    }
}

