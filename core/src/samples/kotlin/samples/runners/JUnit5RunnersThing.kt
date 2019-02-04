package samples.runners

import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext

class JUnit5RunnersThing : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        runnersExample()
    }
}

