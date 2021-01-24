package dev.minutest.junit.engine

import dev.minutest.rootContext
import dev.minutest.test2


fun `root name`() = rootContext("context name") {
    test2("hello") {
// Uncomment to check the test is being run for now
//        org.junit.Assert.fail("")
    }
}