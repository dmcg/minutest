package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*

/**
 * The runtime representation of a context.
 */
internal class PreparedRuntimeContext<PF, F> private constructor(
    override val name: String,
    override val children: List<RuntimeNode>,
    private val befores: List<(F) -> Unit>,
    private val afters: List<(F) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val transforms: List<TestTransform<F>>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val properties: Map<Any, Any>
) : RuntimeContext() {

    companion object {
        operator fun <PF, F> invoke(
            name: String,
            childBuilders: List<NodeBuilder<F, *>>,
            befores: List<(F) -> Unit>,
            afters: List<(F) -> Unit>,
            afterAlls: List<() -> Unit>,
            transforms: List<TestTransform<F>>,
            fixtureFactory: (PF, TestDescriptor) -> F,
            properties: Map<Any, Any>
        ): PreparedRuntimeContext<PF, F> = mutableListOf<RuntimeNode>().let { kids ->
            PreparedRuntimeContext(name, kids, befores, afters, afterAlls, transforms, fixtureFactory, properties).apply {
                kids.addAll(childBuilders.map { it.buildNode() })
            }
        }
    }

    override fun runTest(test: Test<*>, parentFixture: Any, testDescriptor: TestDescriptor): Any {
        return runTestToo(test as Test<F>, parentFixture as PF, testDescriptor) as Any
    }

    private fun runTestToo(test: Test<F>, parentFixture: PF, testDescriptor: TestDescriptor): PF {
        val testWithPreparedFixture: Test<F> = { parentFixture1, testDescriptor1 ->
            applyBeforesTo(parentFixture1)
                .tryMap { f -> test(f, testDescriptor1) }
                .onLastValue(::applyAftersTo)
                .orThrow()
        }
        val transformedTest = applyTransformsTo(testWithPreparedFixture)
        transformedTest.invoke(fixtureFactory(parentFixture, testDescriptor), testDescriptor)
        return parentFixture
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun applyTransformsTo(test: Test<F>): Test<F> =
        transforms.fold(test) { acc, transform -> transform(acc) }

    private fun applyAftersTo(fixture: F) {
        afters.forEach { afterFn ->
            afterFn(fixture)
        }
    }

    private fun copy(
        name: String = this.name,
        children: List<RuntimeNode> = this.children,
        befores: List<(F) -> Unit> = this.befores,
        afters: List<(F) -> Unit> = this.afters,
        afterAlls: List<() -> Unit> = this.afterAlls,
        transforms: List<TestTransform<F>> = this.transforms,
        fixtureFactory: (PF, TestDescriptor) -> F = this.fixtureFactory,
        properties: Map<Any, Any> = this.properties
    ) = PreparedRuntimeContext(name,
        children,
        befores,
        afters,
        afterAlls,
        transforms,
        fixtureFactory,
        properties)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F): OpResult<F> {
        befores.forEach { beforeFn ->
            try {
                beforeFn(fixture)
            } catch (t: Throwable) {
                return OpResult(t, fixture)
            }
        }
        return OpResult(null, fixture)
    }
}

private fun Named.andThen(name: String): Named = object : Named {
    override val name: String = name
    override val parent = this@andThen
}