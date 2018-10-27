package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext

internal interface Node {
    val name: String
    fun toRuntimeNode(): RuntimeNode
}

internal class MinuTest<F>(
    override val name: String,
    val context: ParentContext<F>,
    val f: F.() -> F
) : Test<F>, Node {
    
    override fun invoke(fixture: F): F =
        f(fixture)
    
    override fun toRuntimeNode() =
        RuntimeTest(this.name) { context.runTest(f) }
}

interface ParentContext<F> {
    val name: String
    fun runTest(test: F.() -> F)
}

object RootContext : ParentContext<Unit> {
    override val name: String = ""
    override fun runTest(test: Unit.() -> Unit) = test(Unit)
}

internal class MiContext<PF, F>(
    override val name: String,
    private val parent: ParentContext<PF>,
    private var fixtureFn: (PF.() -> F)? = null
) : TestContext<PF, F>, ParentContext<F>, Node {
    
    private var fixtureExplicitySet = false
    private val children = mutableListOf<Node>()
    private val operations = MutableOperations<F>()
    
    override fun fixture(factory: PF.() -> F) {
        if (fixtureExplicitySet) {
            throw IllegalStateException("fixture already set in context \"$name\"")
        }
        fixtureFn = factory
        fixtureExplicitySet = true
    }
    
    override fun before(transform: F.() -> Unit) {
        operations.befores.add(transform)
    }
    
    override fun after(transform: F.() -> Unit) {
        operations.afters.add(transform)
    }
    
    override fun test_(name: String, f: F.() -> F) {
        MinuTest(name, this, f).also { children.add(it) }
    }
    
    override fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }
    
    override fun <G> derivedContext(name: String, fixtureFn: (F.() -> G)?, builder: TestContext<F, G>.() -> Unit) {
        val subContext = MiContext(name, this, fixtureFn)
        subContext.also {
            it.builder()
            children.add(it)
        }
    }

    override fun runTest(test: F.() -> F) {
        fun decoratedTest(parentFixture: PF): PF =
            parentFixture.also {
                operations.applyBeforesTo(createFixtureFrom(parentFixture))
                    .tryMap(test)
                    .also { result ->
                        operations.applyAftersTo(result.lastValue)
                        result.maybeThrow()
                    }
            }
        parent.runTest(::decoratedTest)
    }

    private fun createFixtureFrom(parentFixture: PF): F {
        val fixtureFactory = fixtureFn
            ?: throw IllegalStateException("fixture has not been set in context \"$name\"")
        return fixtureFactory(parentFixture)
    }

    override fun toRuntimeNode(): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map { it.toRuntimeNode() }
    )

    internal fun path(): List<MiContext<*, *>> = generateSequence(this as MiContext<*, *>) { it.parent as? MiContext<*, *> }.toList().reversed()
}

/**
 * Build a test context out of context.
 */
internal fun <F> topContext(name: String, fixtureFn: (Unit.() -> F)? = null, builder: TestContext<Unit, F>.() -> Unit) =
    MiContext(name, RootContext, fixtureFn).apply { builder() }