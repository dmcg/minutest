// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExampleMinutest")

package samples.minutestRunner.a

import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

fun `example context`() = rootContext<Unit> {
    test("a failing test") {
        fail("example failure")
    }
    
    test("a passing test") {
        assertTrue(true, "example success")
    }
}

