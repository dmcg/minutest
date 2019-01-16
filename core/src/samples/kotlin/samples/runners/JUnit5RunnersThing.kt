package samples.runners

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext

class JUnit5RunnersThing : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        runnersExample()
    }
}

