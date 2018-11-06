package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(test: Test<Unit>) = test(Unit)
}

fun <F> topLevelContext(
    name: String,
    type: KType,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    ContextBuilder<Unit, F>(
        name,
        type,
        fixtureFactoryFor(type.classifier == Unit::class),
        false
    ).apply(builder)

fun <F> topLevelContext(
    name: String,
    type: KType,
    fixture: F,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    topLevelContext<F>(name, type, { _, _ -> fixture }, builder)

fun <F> topLevelContext(
    name: String,
    type: KType,
    fixtureFactory: ((Unit, TestDescriptor) -> F)?,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    ContextBuilder(name, type, fixtureFactory, true).apply(builder)


@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(isUnit: Boolean): ((Unit, TestDescriptor) -> F)? = if (isUnit) {{ _, _ -> Unit as F }} else null

fun KClass<*>.asKType(isNullable: Boolean) =  object : KType {
    override val arguments: List<KTypeProjection> = emptyList()
    override val classifier: KClassifier = this@asKType
    override val isMarkedNullable = isNullable
    override fun toString() = this@asKType.toString()
}

inline fun <reified G> asKType() = G::class.asKType(null is G)

