package samples.runners

import dev.minutest.junit.JUnit4Minutests
import dev.minutest.rootContext

class JUnit4RunnersThing : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        runnersExample()
    }
}

