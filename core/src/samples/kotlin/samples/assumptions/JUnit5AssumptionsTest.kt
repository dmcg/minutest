package samples.assumptions

import org.junit.jupiter.api.Assumptions
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext

class JUnit5AssumptionsTest : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        assumptionsContract(Assumptions::assumeTrue)
    }
}

