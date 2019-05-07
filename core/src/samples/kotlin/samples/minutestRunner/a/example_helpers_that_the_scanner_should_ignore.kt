package samples.minutestRunner.a

import dev.minutest.rootContext


// This should not be picked up the the classpath scanner
@Suppress("unused", "UNUSED_PARAMETER")
fun exampleContextHelper(exampleParam: Any) = rootContext {

}
