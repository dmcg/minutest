package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.Tests
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection


fun <F> topLevelContext(
    name: String,
    type: KType,
    builder: Context<Unit, F>.() -> Unit
): Tests =
    ContextBuilder<Unit, F>(
        name,
        type,
        fixtureFactoryFor(type.classifier == Unit::class),
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
    (this as ContextBuilder<Unit, F>).toTestNode(RootContext)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(isUnit: Boolean): ((Unit, TestDescriptor) -> F)? = if (isUnit) {{ _, _ -> Unit as F }} else null

fun KClass<*>.asKType(isNullable: Boolean) =  object : KType {
    override val arguments: List<KTypeProjection> = emptyList()
    override val classifier: KClassifier = this@asKType
    override val isMarkedNullable = isNullable
    override fun toString() = this@asKType.toString()
}

inline fun <reified G> asKType() = G::class.asKType(null is G)

