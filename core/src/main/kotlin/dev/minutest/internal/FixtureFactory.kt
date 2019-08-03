package dev.minutest.internal

import dev.minutest.TestDescriptor

/**
 * Converts a parent fixture to a child fixture.
 */
internal sealed class FixtureFactory<PF, F>(
    val inputType: FixtureType<PF>,
    val outputType: FixtureType<F>,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f {

    override fun toString() = "FixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"

    fun isCompatibleWith(inputType: FixtureType<*>, outputType: FixtureType<*>) =
        inputType.isSubtypeOf(this.inputType) && this.outputType.isSubtypeOf(outputType)
}

/**
 * Passes parent fixture on unchanged where this is known safe.
 */
internal class IdFixtureFactory<F>(
    inputType: FixtureType<F>
) : FixtureFactory<F, F>(inputType, inputType, { pf, _ -> pf }) {

    override fun toString() = "IdFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

/**
 * Explicitly set by a 'fixture' or 'deriveFixture' block.
 */
internal class ExplicitFixtureFactory<PF, F>(
    inputType: FixtureType<PF>,
    outputType: FixtureType<F>,
    f: (PF, TestDescriptor) -> F
) : FixtureFactory<PF, F>(inputType, outputType, f) {

    override fun toString() = "ExplicitFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

/**
 * Used where we are passing the parent fixture on unchanged but this may not be safe.
 *
 * In this case the context must provide a fixture block.
 */
@Suppress("UNCHECKED_CAST")
internal class UnsafeFixtureFactory<PF, F>(
    inputType: FixtureType<PF>
) : FixtureFactory<PF, F>(inputType, inputType as FixtureType<F>, { pf, _ -> pf as F}) {

    override fun toString() = "UnsafeFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

