package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.TestDescriptor


fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type), explicitFixtureFactory = false)
        .apply(builder)

fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    fixture: F,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    topLevelContext<F>(name, type, { _, _ -> fixture }, builder)

fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    fixtureFactory: ((Unit, TestDescriptor) -> F)?,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    ContextBuilder(name, type, fixtureFactory, explicitFixtureFactory = true)
        .apply(builder)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    }
    else null

