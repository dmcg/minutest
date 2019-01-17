package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.RuntimeNodeTransform
import com.oneeyedmen.minutest.experimental.TestAnnotation


/**
 * RuntimeNodes form a tree of [RuntimeContext]s and [RuntimeTest]s.
 *
 * The generic type [F] is the type of the fixture that will be supplied *to* the node.
 */
@Suppress("unused")
sealed class RuntimeNode<F> {
    abstract val name: String
    abstract val annotations: List<TestAnnotation>
    
    abstract fun withTransformedChildren(transform: RuntimeNodeTransform): RuntimeNode<F>
}

/**
 * A container for [RuntimeNode]s, which are accessed as [RuntimeContext.children].
 *
 * The generic type [PF] is the parent fixture type. [F] is the type of the children.
 */
abstract class RuntimeContext<PF, F> : RuntimeNode<PF>(), AutoCloseable {
    abstract val children: List<RuntimeNode<F>>

    /**
     * Invoke a [Test], converting a parent fixture [PF] to the type required by the test.
     */
    abstract fun runTest(test: Test<F>, parentFixture: PF, testDescriptor: TestDescriptor): F
    abstract fun withChildren(children: List<RuntimeNode<F>>): RuntimeContext<PF, F>
    
    override fun withTransformedChildren(transform: RuntimeNodeTransform) =
        this.withChildren(children.map { transform.applyTo(it) })
}

/**
 * A [Test] with additional name and properties.
 */
data class RuntimeTest<F>(
    override val name: String,
    override val annotations: List<TestAnnotation>,
    private val f: Test<F>
) : RuntimeNode<F>(), Test<F> by f {
    
    override fun withTransformedChildren(transform: RuntimeNodeTransform) = this
}
