package dev.minutest.internal

import dev.minutest.TestDescriptor

/**
 * Converts a parent fixture to a child fixture.
 */
internal class FixtureFactory<PF, F>(
    val inputType: FixtureType,
    val outputType: FixtureType,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f
