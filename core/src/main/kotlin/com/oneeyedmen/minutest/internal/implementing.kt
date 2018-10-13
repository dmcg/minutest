package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import kotlin.reflect.KClass

@Suppress("unused")
internal sealed class Node<F : Any>(val name: String, val fixtureType: KClass<F>)

internal class MinuTest<F : Any>(
    name: String,
    fixtureType: KClass<F>,
    val f: F.() -> F
) : Test<F>, Node<F>(name, fixtureType) {
    override fun invoke(fixture: F): F = f(fixture)
}

internal class MiContext<F : Any>(
    name: String,
    fixtureType: KClass<F>,
    builder: MiContext<F>.() -> Unit
) : TestContext<F>, Node<F>(name, fixtureType) {

    internal val children = mutableListOf<Node<F>>()
    internal val operations = MutableOperations<F>()

    init {
        this.builder()
    }

    override fun before_(transform: F.() -> F) {
        operations.befores.add(transform)
    }

    override fun before(transform: F.() -> Unit) = before_ { this.apply(transform) }

    override fun after(transform: F.() -> Unit) = after_ { this.apply(transform) }

    override fun after_(transform: F.() -> F) {
        operations.afters.add(transform)
    }

    override fun test_(name: String, f: F.() -> F) {
        MinuTest(name, fixtureType, f).also { children.add(it) }
    }

    override fun test(name: String, f: F.() -> Unit) {
        test_(name) {
            apply { f(this) }
        }
    }

    override fun context(name: String, builder: TestContext<F>.() -> Unit) =
        MiContext(name, fixtureType, builder).also { children.add(it) }

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
}

