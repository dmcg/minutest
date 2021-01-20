package samples.minutestRunner.a

import dev.minutest.rootContext
import org.junit.platform.commons.annotation.Testable


// This should not be picked up the the classpath scanner
@Suppress("unused", "UNUSED_PARAMETER")
@Testable
fun exampleContextHelper(exampleParam: Any) = rootContext {

}
