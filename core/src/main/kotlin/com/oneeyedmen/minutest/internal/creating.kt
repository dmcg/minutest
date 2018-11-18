package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.Tests
import kotlin.reflect.KType


fun <F> topLevelContext(
    name: String,
    type: KType,
    builder: Context<Unit, F>.() -> Unit
): Tests =
    ContextBuilder<Unit, F>(
        name,
        type,
        fixtureFactoryFor(type),
        false
    ).apply(builder).toRootTestNode()

fun <F> topLevelContext(
    name: String,
    type: KType,
    fixture: F,
    builder: Context<Unit, F>.() -> Unit
): Tests =
    topLevelContext<F>(name, type, { _, _ -> fixture }, builder)

fun <F> topLevelContext(
    name: String,
    type: KType,
    fixtureFactory: ((Unit, TestDescriptor) -> F)?,
    builder: Context<Unit, F>.() -> Unit
): Tests =
    ContextBuilder(name, type, fixtureFactory, true).apply(builder).toRootTestNode()

private fun <F> Context<Unit, F>.toRootTestNode(): com.oneeyedmen.minutest.Tests =
    (this as ContextBuilder<Unit, F>).toRuntimeNode(RootContext)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: KType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    }
    else null

