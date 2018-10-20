package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.DerivedContext
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

@Suppress("unused")
internal sealed class Node<F>(val name: String) {
    abstract fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>): RuntimeNode
}

internal class MinuTest<F>(
    name: String,
    val f: F.() -> F
) : Test<F>, Node<F>(name) {
    override fun invoke(fixture: F): F = f(fixture)

    override fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>) =
        RuntimeTest(this.name) {
            parent?.runTest(this, parentOperations) ?: error("Test $name has no parent context")
        }
}

internal class MiContext<PF, F>(
    name: String,
    override val parent: TestContext<*>?,
    private val fixtureType: KType
) : TestContext<F>, DerivedContext<PF, F>, Node<F>(name) {

    private val children = mutableListOf<Node<F>>()
    private val operations = MutableOperations<F>()

    override fun before_(transform: F.() -> F) {
        operations.befores.add(transform)
    }

    override fun before(transform: F.() -> Unit) = before_ { this.apply(transform) }

    override fun after(transform: F.() -> Unit) = after_ { this.apply(transform) }

    override fun after_(transform: F.() -> F) {
        operations.afters.add(transform)
    }

    override fun test_(name: String, f: F.() -> F) {
        MinuTest(name, f).also { children.add(it) }
    }

    override fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }


    override fun context(name: String, builder: TestContext<F>.() -> Unit) =
        MiContext<F, F>(name, this, fixtureType).also {
            it.builder()
            children.add(it)
        }

    @Suppress("UNCHECKED_CAST")
    override fun <F2> derivedContext(
        name: String,
        fixtureType: KType,
        builder: DerivedContext<F, F2>.() -> Unit) =
        MiContext<F, F2>(name, this, fixtureType).also {
            it.builder()
            children.add(it as Node<F>)
        }

    override fun addTransform(testTransform: (Test<F>) -> Test<F>) {
        operations.transforms.add(testTransform)
    }

    fun runTest(myTest: Test<F>, parentOperations: Operations<F>) {
        val combinedOperations = parentOperations + operations
        val beforeResult = beforeResultOrThrow(combinedOperations)
        val nextResult = beforeResult.flatMap { fixture ->
            try {
                val transformedTests = combinedOperations.applyTransformsTo(myTest)
                OpResult(null, transformedTests.invoke(fixture))
            } catch (t: Throwable) {
                OpResult(t, fixture)
            }
        }
        combinedOperations.applyAftersTo(nextResult.lastValue)
        nextResult.orThrow()
    }

    @Suppress("UNCHECKED_CAST")
    /**
     * Applies all the befores to Unit and sees whether the result is they type we want. This checks if the combination of
     * the fixture calls works out at runtime.
     */
    private fun beforeResultOrThrow(combinedOperations: Operations<F>): OpResult<F> =
        combinedOperations.applyBeforesTo(Unit as F).also {
            if (!fixtureType.isCompatibleWith(it.lastValue) && (exceptionWasProbablyNotThrownByCodeInBefores(it.t))) {
                error("You need to set a fixture by calling fixture(...)")
            } else {
                // either we have a correct fixture type, or we don't but it is because of a
            }
        }


    override fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map {
            it.toRuntimeNode(this, parentOperations + (parent?.operations ?: Operations.empty()))
        }
    )
}

private fun <F> KType.isCompatibleWith(fixtureValue: F): Boolean {
    return when {
        this.isMarkedNullable && fixtureValue == null -> true
        else -> (classifier as? KClass<*>)?.isInstance(fixtureValue) == true
    }
}

private fun exceptionWasProbablyNotThrownByCodeInBefores(throwable: Throwable?) =
    throwable == null || (throwable is ClassCastException && (throwable.message?.contains("Unit") == true))

fun KClass<*>.asKType(isNullable: Boolean) =  object : KType {
    override val arguments: List<KTypeProjection> = emptyList()
    override val classifier: KClassifier = this@asKType
    override val isMarkedNullable = isNullable
}