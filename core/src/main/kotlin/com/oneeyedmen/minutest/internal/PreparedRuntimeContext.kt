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

    override fun runTest(test: Test<*>, parentContext: ParentContext<*>) {
        (parentContext as ParentContext<PF>).runTest(buildParentTest(test as Test<F>, parentContext))
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun buildParentTest(test: Test<F>, parentContext: ParentContext<PF>): Test<PF> {

        // The issue here is that as the invocation climbs up the parentContext stack, we loose bits of the test name
        // So we latch at the longest name that we see.
        val testPath: Named = object : Named {
            override val name: String = test.name
            override val parent = parentContext.andThen(this@PreparedRuntimeContext)
        }
        val deepestTestPath: Named = if (testPath.fullName().size > test.fullName().size)
            testPath else test

        val testWithPreparedFixture = object : Test<F>, Named by deepestTestPath {
            override fun invoke(initialFixture: F, testDescriptor: TestDescriptor) =
                applyBeforesTo(initialFixture)
                    .tryMap { f -> test(f, testDescriptor) }
                    .onLastValue(::applyAftersTo)
                    .orThrow()
        }

        return object : Test<PF>, Named by deepestTestPath {
            override fun invoke(parentFixture: PF, testDescriptor: TestDescriptor): PF {
                val transformedTest = applyTransformsTo(testWithPreparedFixture)
                transformedTest.invoke(fixtureFactory(parentFixture, deepestTestPath), testDescriptor)
                return parentFixture
            }
        }
    }

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

    // TODO - make this a List<NodeBuilder> to make sure that we preserve the parent-child relationship
    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)
}
