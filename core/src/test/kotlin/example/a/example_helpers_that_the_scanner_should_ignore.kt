package example.a

import com.oneeyedmen.minutest.experimental.context


// This should not be picked up the the classpath scanner
fun exampleContextHelper(exampleParam: Any) = context<Unit> {

}
