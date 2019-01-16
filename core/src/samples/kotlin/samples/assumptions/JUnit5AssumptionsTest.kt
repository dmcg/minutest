package samples.assumptions

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assumptions

class JUnit5AssumptionsTest : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        assumptionsContract(Assumptions::assumeTrue)
    }
}

