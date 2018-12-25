package example.a

import com.oneeyedmen.minutest.rootContext


// This should not be picked up the the classpath scanner
@Suppress("unused", "UNUSED_PARAMETER")
fun exampleContextHelper(exampleParam: Any) = rootContext<Unit> {

}
