package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal class Operations<F>(
    val befores: MutableList<(F) -> F> = mutableListOf(),
    val transforms: MutableList<(Test<F>) -> Test<F>> = mutableListOf(),
    val afters: MutableList<(F) -> F> = mutableListOf()
) {
    operator fun plus(subordinate: Operations<F>) = Operations(
        befores = (befores + subordinate.befores).toMutableList(),
        transforms = (transforms + subordinate.transforms).toMutableList(),
        afters = (subordinate.afters + afters).toMutableList() // we apply parent afters after child
    )

    fun applyBeforesTo(fixture: F) = befores.fold(fixture) { acc, transform -> transform(acc) }

    fun applyTransformsTo(test: Test<F>) = transforms.fold(test) { acc, transform -> transform(acc) }

    fun applyAftersTo(fixture: F) = afters.fold(fixture) { acc, transform -> transform(acc) }
}