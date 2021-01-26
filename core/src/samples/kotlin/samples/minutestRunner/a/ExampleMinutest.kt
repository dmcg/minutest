// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExampleMinutest")

package samples.minutestRunner.a

import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.platform.commons.annotation.Testable

@Testable
fun `example context`() = rootContext {
    test("a failing test") {
        fail("example failure")
    }

    test("a passing test") {
        assertTrue(true, "example success")
    }
}

