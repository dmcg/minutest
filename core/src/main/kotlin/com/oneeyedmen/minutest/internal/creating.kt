package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Test
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
        name = name,
        type = type,
        fixtureFactory = null,
        explicitFixtureFactory = false
    ).apply(builder)


fun <F> topLevelContext(
    name: String,
    type: KType,
    fixtureBuilder: (Unit.() -> F)?,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    ContextBuilder(
        name = name,
        type = type,
        fixtureFactory = fixtureBuilder,
        explicitFixtureFactory = true
    ).apply(builder)

fun <F> topLevelContext(
    name: String,
    type: KType,
    fixture: F,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    topLevelContext<F>(name, type, { fixture }, builder)


fun KClass<*>.asKType(isNullable: Boolean) = object : KType {
    override val arguments: List<KTypeProjection> = emptyList()
    override val classifier: KClassifier = this@asKType
    override val isMarkedNullable = isNullable
    override fun toString() = this@asKType.toString()
}

inline fun <reified G> asKType() = G::class.asKType(null is G)

