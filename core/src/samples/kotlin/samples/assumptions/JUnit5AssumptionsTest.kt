package samples.assumptions

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assumptions

class JUnit5AssumptionsTest : JUnit5Minutests {

    fun tests() = rootContext {
        assumptionsContract(Assumptions::assumeTrue)
    }
}

