package samples.runners

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

class JUnit5RunnersThing : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        runnersExample()
    }
}

