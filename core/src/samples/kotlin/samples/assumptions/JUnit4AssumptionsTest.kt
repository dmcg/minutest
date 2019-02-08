package samples.assumptions

import dev.minutest.junit.JUnit4Minutests
import dev.minutest.rootContext
import org.junit.Assume

class JUnit4AssumptionsTest : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        assumptionsContract(Assume::assumeTrue)
    }
}

