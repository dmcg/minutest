package com.oneeyedmen.minutest

typealias TestTransform<F> = (MinuTest<F>) -> MinuTest<F>

internal class MiContext<F>(
    name: String,
    builder: MiContext<F>.() -> Unit
) : TestContext<F>(name) {

    internal val children = mutableListOf<Node<F>>()
    override val operations = Operations<F>()

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
        MiContext(name, builder).also { children.add(it) }

    override fun addTransform(testTransform: TestTransform<F>) {
        operations.transforms.add(testTransform)
    }

    @Suppress("UNCHECKED_CAST")
    fun runTest(myTest: MinuTest<F>, parentOperations: Operations<F>) {
        try {
            val combinedOperations = parentOperations + operations
            val transformedFixture = combinedOperations.applyBeforesTo(Unit as F)
            val transformedTest = combinedOperations.applyTransformsTo(myTest)
            try {
                val resultFixture = transformedTest.f(transformedFixture)
                combinedOperations.applyAftersTo(resultFixture)
            } catch (t: Throwable) {
                // TODO - this may result in double afters
                combinedOperations.applyAftersTo(transformedFixture)
                throw t
            }
        } catch (x: ClassCastException) {
            // Provided a fixture has been set, the Unit never makes it as far as any functions that cast it to F, so
            // this works. And if the type of F is Unit, you don't need to set a fixture, as the Unit will do. Simples.
            error("You need to set a fixture by calling fixture(...)")
        }
    }
}



internal class Operations<F>(
    val befores: MutableList<(F) -> F> = mutableListOf(),
    val transforms: MutableList<TestTransform<F>> = mutableListOf(),
    val afters: MutableList<(F) -> F> = mutableListOf()
) {
    operator fun plus(subordinate: Operations<F>) = Operations(
        befores = (befores + subordinate.befores).toMutableList(),
        transforms = (transforms + subordinate.transforms).toMutableList(),
        afters = (subordinate.afters + afters).toMutableList() // we apply parent afters after child
    )

    fun applyBeforesTo(fixture: F) = befores.fold(fixture) { acc, transform -> transform(acc) }

    fun applyTransformsTo(test: MinuTest<F>) = transforms.fold(test) { acc, transform -> transform(acc) }

    fun applyAftersTo(fixture: F) = afters.fold(fixture) { acc, transform -> transform(acc) }
}