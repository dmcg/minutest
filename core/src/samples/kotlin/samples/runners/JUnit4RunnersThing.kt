package samples.runners

import uk.org.minutest.junit.JUnit4Minutests
import uk.org.minutest.rootContext

class JUnit4RunnersThing : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        runnersExample()
    }
}

