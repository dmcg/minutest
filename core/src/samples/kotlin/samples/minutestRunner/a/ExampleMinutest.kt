// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExampleMinutest")

package samples.minutestRunner.a

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import uk.org.minutest.rootContext

fun `example context`() = rootContext<Unit> {
    test("a failing test") {
        fail("example failure")
    }
    
    test("a passing test") {
        assertTrue(true, "example success")
    }
}

