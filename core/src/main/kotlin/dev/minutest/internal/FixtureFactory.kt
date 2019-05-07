package dev.minutest.internal

import dev.minutest.TestDescriptor

internal class FixtureFactory<PF, F>(
    val type: FixtureType,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f

@Suppress("UNCHECKED_CAST") // safe as long as we make sure that we fail if the fixture is accessed before it is redefined
internal fun <PF, F> rootFixtureFactoryHack() = rootFixtureFactory as FixtureFactory<PF, F>

internal val rootFixtureFactory = FixtureFactory<Unit, Unit>(FixtureType(Unit::class, false)) { _, _ -> Unit }
