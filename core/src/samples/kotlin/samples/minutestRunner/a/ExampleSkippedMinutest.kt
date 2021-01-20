// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExampleSkippedMinutest")

package samples.minutestRunner.a

import dev.minutest.experimental.SKIP
import dev.minutest.experimental.minus
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.fail
import org.junit.platform.commons.annotation.Testable

@Testable
fun `example skipped context`() = SKIP - rootContext {
    test("skip is honoured") {
        fail("skip wasn't honoured")
    }
}

