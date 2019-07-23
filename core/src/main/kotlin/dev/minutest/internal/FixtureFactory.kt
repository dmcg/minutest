package dev.minutest.internal

import dev.minutest.TestDescriptor

/**
 * Converts a parent fixture to a child fixture.
 */
internal open class FixtureFactory<PF, F>(
    val inputType: FixtureType,
    val outputType: FixtureType,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f {

    override fun toString() = "FixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

internal class UnsafeFixtureFactory<PF, F>(
    inputType: FixtureType
) : FixtureFactory<PF, F>(inputType, inputType, { pf, _ -> pf as F}) {

    override fun toString() = "UnsafeFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

internal class ExplicitFixtureFactory<PF, F>(
    inputType: FixtureType,
    outputType: FixtureType,
    f: (PF, TestDescriptor) -> F
) : FixtureFactory<PF, F>(inputType, outputType, f) {

    override fun toString() = "ExplicitFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}

internal class IdFixtureFactory<F>(
    inputType: FixtureType
) : FixtureFactory<F, F>(inputType, inputType, { pf, _ -> pf }) {

    override fun toString() = "IdFixtureFactory((${inputType.qualifiedName}) -> ${outputType.qualifiedName})"
}