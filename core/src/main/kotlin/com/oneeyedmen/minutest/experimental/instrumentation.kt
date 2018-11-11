package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestDescriptor


/**
 * Define the fixture that will be used in this context's tests and sub-contexts by
 * transforming the parent fixture, accessible as the receiver 'this'.
 *
 * Information on the current test is available as 'testDescriptor'.
 */
fun <ParentF, F> Context<ParentF, F>.deriveFixtureInstrumented(f: (ParentF).(testDescriptor: TestDescriptor) -> F) = privateDeriveFixture(f)

/**
 * Define the fixture that will be used in this context's tests and sub-contexts.
 *
 * Information on the current test is available as 'testDescriptor'.
 */
fun <ParentF, F> Context<ParentF, F>.fixtureInstrumented(factory: (Unit).(testDescriptor: TestDescriptor) -> F) = privateDeriveFixture { Unit.factory(it) }