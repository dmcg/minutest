
package samples.minutestRunner.a

import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.commons.annotation.Testable

class ExampleMinutestInClass {

    @Testable
    fun `example context in class`() = rootContext {
        test("a passing test") {
            assertTrue(true, "example success")
        }
    }
}