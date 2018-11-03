package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestTransform

internal class MiContext<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    private var fixtureFn: (PF.() -> F)? = null
) : Context<PF, F>, ParentContext<F>, Node, Named {
    
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
    
    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    override fun context(name: String, builder: Context<F, F>.() -> Unit) {
        createSubContext(name, { this }, builder)
    }
    
    override fun <G> derivedContext(name: String, builder: Context<F, G>.() -> Unit) {
        createSubContext(name, null, builder)
    }
    
    private fun <G> createSubContext(name: String, fixtureFn: (F.() -> G)?, builder: Context<F, G>.() -> Unit) {
        val subContext = MiContext(name, this, fixtureFn)
        subContext.also {
            it.builder()
            children.add(it)
        }
    }
    
    override fun addTransform(transform: TestTransform<F>) {
        operations.transforms += transform
    }

    fun toMiContext2(parent: ParentContext<PF>): MiContext2<PF, F> {
        return MiContext2(
            name,
            parent,
            fixtureFn ?: error("Fixture has not been set in context \"$name\""),
            emptyList(),
            operations
        ).apply {
            this.children = this@MiContext.children.map { child ->
                when (child) {
                    is MiContext<*, *> -> (child as MiContext<F, *>).toMiContext2(this) as Node
                    is MinuTest<*> -> MinuTest(child.name, this, child as MinuTest<F>)
                    else -> error("Unexpected child $child")
                }
            }
        }
    }
    
    override fun runTest(test: Test<F>) {
        val testWithPreparedFixture = object : Test<F>, Named by test {
            override fun invoke(initialFixture: F) =
                operations.applyBeforesTo(initialFixture)
                    .tryMap(test)
                    .onLastValue(operations::applyAftersTo)
                    .orThrow()
        }
        
        val testInParent = object : Test<PF>, Named by test {
            override fun invoke(parentFixture: PF): PF {
                val transformedTest = operations.applyTransformsTo(testWithPreparedFixture)
                val initialFixture = createFixtureFrom(parentFixture)
                transformedTest(initialFixture)
                return parentFixture
            }
        }
        
        parent.runTest(testInParent)
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