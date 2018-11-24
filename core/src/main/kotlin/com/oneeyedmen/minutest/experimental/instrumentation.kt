package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.internal.ContextBuilder


/**
 * Define the fixture that will be used in this context's tests and sub-contexts by
 * transforming the parent fixture, accessible as the receiver 'this'.
 *
 * Information on the current test is available as 'testDescriptor'.
 */
fun <ParentF, F> Context<ParentF, F>.deriveFixtureInstrumented(f: (ParentF).(testDescriptor: TestDescriptor) -> F) =
    (this as ContextBuilder<ParentF, F>).deriveInstrumentedFixture(f)

/**
 * Define the fixture that will be used in this context's tests and sub-contexts.
 *
 * Information on the current test is available as 'testDescriptor'.
 */
fun <ParentF, F> Context<ParentF, F>.fixtureInstrumented(factory: (Unit).(testDescriptor: TestDescriptor) -> F) =
    (this as ContextBuilder<ParentF, F>).deriveInstrumentedFixture { _, testDescriptor ->  Unit.factory(testDescriptor) }