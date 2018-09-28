package com.oneeyedmen.minutest

internal class MiContext<F>(
    name: String,
    childTransforms: List<(MinuTest<F>) -> MinuTest<F>> = emptyList(),
    builder: MiContext<F>.() -> Unit
) : TestContext<F>(name){

    internal val children = mutableListOf<Node<F>>()
    private val childTransforms = childTransforms.toMutableList()

    init {
        this.builder()
    }

    override fun fixture(factory: () -> F) {
        before_ {
            factory()
        }
    }

    override fun modifyFixture(transform: F.() -> Unit) {
        before(transform)
    }

    override fun replaceFixture(transform: F.() -> F) {
        before_(transform)
    }

    override fun test(name: String, f: F.() -> Unit) = test_(name) {
        apply { f(this) }
    }

    override fun test_(name: String, f: F.() -> F) = MinuTest(name, f).also { children.add(it) }

    override fun context(name: String, builder: TestContext<F>.() -> Unit) =
        MiContext(name, childTransforms, builder).also { children.add(it) }

    override fun addTransform(testTransform: (MinuTest<F>) -> MinuTest<F>) { childTransforms.add(testTransform) }

    @Suppress("UNCHECKED_CAST")
    fun runTest(test: MinuTest<F>) {
        try {
            test.f(Unit as F)
        } catch (x: ClassCastException) {
            // Provided a fixture has been set, the Unit never makes it as far as any functions that cast it to F, so
            // this works. And if the type of F is Unit, you don't need to set a fixture, as the Unit will do. Simples.
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    fun applyTransformsTo(baseNode: Node<F>): Node<F> = childTransforms.reversed().fold(baseNode) { node, transform ->
        when (node) {
            is MinuTest<F> -> transform(node)
            else -> node
        }
    }
}