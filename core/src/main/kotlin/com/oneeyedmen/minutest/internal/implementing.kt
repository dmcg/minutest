package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.DerivedContext
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import kotlin.reflect.KClass

@Suppress("unused")
internal sealed class Node<F : Any>(val name: String) {
    abstract fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>): RuntimeNode
}

internal class MinuTest<F : Any>(
    name: String,
    val f: F.() -> F
) : Test<F>, Node<F>(name) {
    override fun invoke(fixture: F): F = f(fixture)

    override fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>) =
        RuntimeTest(this.name) {
            parent?.runTest(this, parentOperations) ?: error("Test $name has no parent context")
        }
}

internal class MiContext<PF: Any, F : Any>(
    name: String,
    private val fixtureType: KClass<F>
) : TestContext<F>, DerivedContext<PF, F>, Node<F>(name) {

    internal val children = mutableListOf<Node<F>>()
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
        MiContext<F, F>(name, fixtureType).also {
            it.builder()
            children.add(it)
        }

    @Suppress("UNCHECKED_CAST")
    override fun <F2: Any> derivedContext(
        name: String,
        fixtureType: KClass<F2>,
        builder: DerivedContext<F, F2>.() -> Unit) =
        MiContext<F, F2>(name, fixtureType).also {
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
            if (!(fixtureType.isInstance(it.lastValue)))
                error("You need to set a fixture by calling fixture(...)")
        }

    override fun toRuntimeNode(parent: MiContext<*, F>?, parentOperations: Operations<F>): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map {
            it.toRuntimeNode(this, parentOperations + (parent?.operations ?: Operations.empty()))
        }
    )
}

