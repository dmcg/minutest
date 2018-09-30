package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

interface Operations<F> {
    val befores: List<(F) -> F>
    val transforms: List<(Test<F>) -> Test<F>>
    val afters: List<(F) -> F>

    fun applyBeforesTo(fixture: F) = befores.fold(fixture) { acc, transform -> transform(acc) }

    fun applyTransformsTo(test: Test<F>) = transforms.fold(test) { acc, transform -> transform(acc) }

    fun applyAftersTo(fixture: F) = afters.fold(fixture) { acc, transform -> transform(acc) }

    operator fun plus(subordinate: Operations<F>): Operations<F> = ImmutableOperations(
        befores = (befores + subordinate.befores),
        transforms = (transforms + subordinate.transforms),
        afters = (subordinate.afters + afters) // we apply parent afters after child
    )

    companion object {
        fun <F> empty(): Operations<F> = ImmutableOperations<F>(emptyList(), emptyList(), emptyList())
    }
}

internal class MutableOperations<F>(
    override val befores: MutableList<(F) -> F> = mutableListOf(),
    override val transforms: MutableList<(Test<F>) -> Test<F>> = mutableListOf(),
    override val afters: MutableList<(F) -> F> = mutableListOf()
) : Operations<F>

internal class ImmutableOperations<F>(
    override val befores: List<(F) -> F>,
    override val transforms: List<(Test<F>) -> Test<F>>,
    override val afters: List<(F) -> F>
) : Operations<F>