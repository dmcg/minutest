package example.b

import com.oneeyedmen.minutest.rootContext

val `this context  should not appear in the test plan` = rootContext<Unit> {
    test("this test should not appear in the test plan") {
    
    }
}
