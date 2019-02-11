package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

class JUnit5RunnersThing : JUnit5Minutests {

    fun tests() = rootContext<Unit> {
        runnersExample()
    }
}

