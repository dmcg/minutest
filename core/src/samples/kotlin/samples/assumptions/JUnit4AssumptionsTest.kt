package samples.assumptions

import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.Assume

class JUnit4AssumptionsTest : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        assumptionsContract(Assume::assumeTrue)
    }
}

