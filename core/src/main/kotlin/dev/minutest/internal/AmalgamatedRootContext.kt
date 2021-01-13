package dev.minutest.internal

import dev.minutest.*

internal data class AmalgamatedRootContext(
    val packageName: String,
    private val contextBuilderBuilders: List<() -> RootContextBuilder>,
    override val markers: List<Any> = emptyList()
) : Context<Unit, Unit>() {

    override val name: String get() = packageName

    override val children: List<Node<Unit>> by lazy {
        contextBuilderBuilders.map { f ->
            f().buildNode()
        }
    }

    override fun runTest(testlet: Testlet<Unit>, parentFixture: Unit, testDescriptor: TestDescriptor) =
        RootExecutor.runTest(testDescriptor, testlet)

    override fun withTransformedChildren(transform: NodeTransform<Unit>): Context<Unit, Unit> {
        TODO("not implemented")
    }

    override fun close() {}
}