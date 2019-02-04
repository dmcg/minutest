package samples.assumptions

import org.junit.Assume
import uk.org.minutest.junit.JUnit4Minutests
import uk.org.minutest.rootContext

class JUnit4AssumptionsTest : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        assumptionsContract(Assume::assumeTrue)
    }
}

