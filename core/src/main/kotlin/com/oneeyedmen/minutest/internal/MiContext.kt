package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context

internal class MiContext<PF, F>(
    override val name: String,
    private val parent: ParentContext<PF>,
    private var fixtureFn: (PF.() -> F)? = null
) : Context<PF, F>, ParentContext<F>, Node {

    private var fixtureCalled = false
    private val children = mutableListOf<Node>()
    private val operations = Operations<F>()

    override fun fixture(factory: PF.() -> F) {
        if (fixtureCalled)
            throw IllegalStateException("fixture already set in context \"$name\"")
        fixtureFn = factory
        fixtureCalled = true
    }

    override fun before(operation: F.() -> Unit) {
        operations.befores.add(operation)
    }

    override fun after(operation: F.() -> Unit) {
        operations.afters.add(operation)
    }

    override fun test_(name: String, f: F.() -> F) {
        MinuTest(name, this, f).also { children.add(it) }
    }

    override fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }

    override fun <G> derivedContext(name: String, fixtureFn: (F.() -> G)?, builder: Context<F, G>.() -> Unit) {
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
        // have to explicitly check rather than elvis because invoking fixtureFn may return null
        val fixtureFactory = fixtureFn ?: {
            throw IllegalStateException("fixture has not been set in context \"$name\"")
        }
        return fixtureFactory(parentFixture)
    }

    override fun toRuntimeNode(): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map { it.toRuntimeNode() }
    )

    // for debugging
    @Suppress("unused")
    fun path(): List<ParentContext<*>> =
        generateSequence(this as MiContext<*, *>) {
            it.parent as? MiContext<*, *>
        }.toList().reversed()
}