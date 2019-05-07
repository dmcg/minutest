package samples.runners

import dev.minutest.junit.experimental.JUnit4Minutests
import dev.minutest.rootContext

class JUnit4Sample : JUnit4Minutests() {

    fun tests() = rootContext {
        runnersExample()
    }
}

