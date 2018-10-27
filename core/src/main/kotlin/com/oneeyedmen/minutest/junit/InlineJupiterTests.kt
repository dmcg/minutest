//package com.oneeyedmen.minutest.junit
//
//import com.oneeyedmen.minutest.TestContext
//import com.oneeyedmen.minutest.internal.asKType
//import com.oneeyedmen.minutest.testContext
//
///**
// * Convenience class to reduce boilerplate
// */
//abstract class InlineJupiterTests<F>(
//    builder: TestContext<Unit, F>.() -> Unit
//) : JupiterTests {
//
//    @Suppress("LeakingThis")
//    override val tests = junitTests<F>(builder)
//}