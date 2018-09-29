package com.oneeyedmen.minutest

typealias TestTransform<F> = (MinuTest<F>) -> MinuTest<F>

internal class MiContext<F>(
    name: String,
    childTransforms: List<TestTransform<F>> = emptyList(),
    builder: MiContext<F>.() -> Unit
) : TestContext<F>(name){

    internal val children = mutableListOf<Node<F>>()
    private val testTransforms = childTransforms.toMutableList()

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
        MiContext(name, testTransforms, builder).also { children.add(it) }

    override fun addTransform(testTransform: TestTransform<F>) { testTransforms.add(testTransform) }

    @Suppress("UNCHECKED_CAST")
    fun runTest(myTest: MinuTest<F>) {
        try {
            applyTransformsTo(myTest).f(Unit as F)
        } catch (x: ClassCastException) {
            // Provided a fixture has been set, the Unit never makes it as far as any functions that cast it to F, so
            // this works. And if the type of F is Unit, you don't need to set a fixture, as the Unit will do. Simples.
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    private fun applyTransformsTo(test: MinuTest<F>) = testTransforms.reversed().fold(test) { node, transform ->
        transform(node)
    }
}