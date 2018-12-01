package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*


inline fun <reified F> transformedTopLevelContext(
    name: String,
    transform: (RuntimeNode) -> RuntimeNode,
    noinline builder: Context<Unit, F>.() -> Unit
): RuntimeNode =
    topLevelContext(name, askType<F>(), builder)
        .buildRootNode()
        .run(transform)

fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type))
        .apply(builder)


@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    }
    else null

