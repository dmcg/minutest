package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import kotlin.test.fail


class AnnotationTests : JupiterTests {

    override val tests = SKIP - context<Unit>(skipAndFocus) {
        MyAnnotation - test("test") {}
        MyAnnotation - context("context") {}
        context("context") {
            annotateWith(MyAnnotation)
        }
        test("top level skip works") {
            fail("top level skip didn't work")
        }
    }
}

object MyAnnotation : TestAnnotation