// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExplicitTypedMinutest")
package example.a

import com.oneeyedmen.minutest.experimental.context
import org.junit.jupiter.api.Assertions
import java.util.Stack

fun `example typed context`() = context<Stack<String>> {
    fixture { Stack() }
    
    test("a typed fixture test") {
        Assertions.assertTrue(isEmpty())
    }
}
