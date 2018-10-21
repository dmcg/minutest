package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.asKType
import kotlin.reflect.KType


inline fun <reified F> testContext(name: String, noinline builder: TestContext<F>.() -> Unit): TestContext<F> =
    testContext(name, F::class.asKType(null is F), builder)

fun <F> testContext(name: String, fixtureType: KType, builder: TestContext<F>.() -> Unit): TestContext<F> =
    MiContext<Unit, F>(name, null, fixtureType).apply { builder() }