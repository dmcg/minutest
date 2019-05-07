package samples.minutestRunner.b

import dev.minutest.rootContext

val `this context  should not appear in the test plan` = rootContext {
    test("this test should not appear in the test plan") {
    
    }
}
