package dev.minutest.internal

import dev.minutest.TestDescriptor


internal class FixtureFactory<PF, F>(
    val type: FixtureType,
    val f: (PF, TestDescriptor) -> F
) : (PF, TestDescriptor) -> F by f


@Suppress("UNCHECKED_CAST") // probably least worst solution
internal fun <PF, F> rootFixtureFactoryHack() = rootFixtureFactory as FixtureFactory<PF, F>

private val rootFixtureFactory = FixtureFactory<Unit, Unit>(FixtureType(Unit::class, false)) { _, _ -> Unit }
