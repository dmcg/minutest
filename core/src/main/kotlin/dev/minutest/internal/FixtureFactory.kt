package dev.minutest.internal

import dev.minutest.TestDescriptor


internal class FixtureFactory<PF, F>(
    val type: FixtureType,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f
