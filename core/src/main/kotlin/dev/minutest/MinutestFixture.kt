package dev.minutest


/**
 * An annotation that you can put on a fixture class to prevent bad DSL structure - eg contexts nested inside
 * tests or tests inside fixtures blocks.
 */
@DslMarker
annotation class MinutestFixture
