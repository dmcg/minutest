package example.runners

import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.rootContext

class JUnit4RunnersThing : JUnit4Minutests() {

    fun tests() = rootContext<Unit> {
        runnersExample()
    }
}

