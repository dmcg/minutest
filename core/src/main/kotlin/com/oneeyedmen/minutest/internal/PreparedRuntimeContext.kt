package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*

/**
 * The runtime representation of a context.
 */
internal class PreparedRuntimeContext<PF, F> private constructor(
    override val name: String,
    override val parent: RuntimeContext<*, PF>?,
    override val children: List<RuntimeNode<F, *>>,
    private val befores: List<(F) -> Unit>,
    private val afters: List<(F) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val transforms: List<TestTransform<F>>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val properties: Map<Any, Any>
) : RuntimeContext<PF, F>() {

    companion object {
        operator fun <PF, F> invoke(
            name: String,
            parent: RuntimeContext<*, PF>?,
            childBuilders: List<NodeBuilder<F, *>>,
            befores: List<(F) -> Unit> = emptyList(),
            afters: List<(F) -> Unit> = emptyList(),
            afterAlls: List<() -> Unit> = emptyList(),
            transforms: List<TestTransform<F>> = emptyList(),
            fixtureFactory: (PF, TestDescriptor) -> F,
            properties: Map<Any, Any> = emptyMap()
        ): PreparedRuntimeContext<PF, F> = mutableListOf<RuntimeNode<F, *>>().let { kids ->
            PreparedRuntimeContext(name, parent, kids, befores, afters, afterAlls, transforms, fixtureFactory, properties).apply {
                kids.addAll(childBuilders.map { it.buildNode(this) })
            }
        }
    }

    override fun runTest(test: Test<F>) {
        val testForParentToRun: Test<PF> = buildParentTest(test)
        if (parent != null)
            parent.runTest(testForParentToRun)
        else {
            // I don't have a parent so I must be a root, so my parent fixture type must be Unit. I can provide that.
            @Suppress("UNCHECKED_CAST")
            (testForParentToRun as Test<Unit>)(Unit)
        }
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun buildParentTest(test: Test<F>): Test<PF> {
        val testWithPreparedFixture = object : Test<F>, Named by test {
            override fun invoke(initialFixture: F) =
                applyBeforesTo(initialFixture)
                    .tryMap(test)
                    .onLastValue(::applyAftersTo)
                    .orThrow()
        }

        return object : Test<PF>, Named by test {
            override fun invoke(parentFixture: PF): PF {
                val transformedTest = applyTransformsTo(testWithPreparedFixture)
                transformedTest(fixtureFactory(parentFixture, this))
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
        parent: RuntimeContext<*, PF>? = this.parent,
        children: List<RuntimeNode<F, *>> = this.children,
        befores: List<(F) -> Unit> = this.befores,
        afters: List<(F) -> Unit> = this.afters,
        afterAlls: List<() -> Unit> = this.afterAlls,
        transforms: List<TestTransform<F>> = this.transforms,
        fixtureFactory: (PF, TestDescriptor) -> F = this.fixtureFactory,
        properties: Map<Any, Any> = this.properties
    ) = PreparedRuntimeContext(name,
        parent,
        children,
        befores,
        afters,
        afterAlls,
        transforms,
        fixtureFactory,
        properties)

    // TODO - make this a List<NodeBuilder> to make sure that we preserve the parent-child relationship
    override fun adopting(children: List<RuntimeNode<F, *>>) = copy(children = children.map { it.adoptedBy(this) } )

    override fun adoptedBy(parent: RuntimeContext<*, PF>) = copy(parent = parent)
}
