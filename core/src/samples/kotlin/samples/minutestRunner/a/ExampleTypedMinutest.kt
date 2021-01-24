// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExplicitTypedMinutest")
package samples.minutestRunner.a

import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions
import org.junit.platform.commons.annotation.Testable
import java.util.*

@Testable
fun `example typed context`() = rootContext<Stack<String>> {
    fixture { Stack() }
    
    test2("a typed fixture test") {
        Assertions.assertTrue(isEmpty())
    }
}
