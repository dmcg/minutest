package com.oneeyedmen.minutest

/**
 * A test with a name that can be invoked on a fixture.
 */
interface Test<F> : (F) -> F {
    val name: String
}

@Suppress("FunctionName")
@MinutestMarker
interface TestContext<ParentF, F> {
    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     */
    fun fixture(factory: ParentF.() -> F)

    fun modifyFixture(transform: F.() -> Unit) = before(transform)
    
    fun before(transform: F.() -> Unit)
    
    fun after(transform: F.() -> Unit)
    
    fun test_(name: String, f: F.() -> F)
    fun test(name: String, f: F.() -> Unit)
    
    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    fun context(name: String, builder: TestContext<F, F>.() -> Unit) {
        derivedContext(name, { this }) {
            builder()
        }
    }
    
    /**
     * Define a sub-context with a different fixture type.
     */
    fun <G> derivedContext(name: String, fixtureFn: (F.() -> G)? = null, builder: TestContext<F, G>.() -> Unit)
}


