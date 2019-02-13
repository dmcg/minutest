package dev.minutest.internal

import dev.minutest.RootContextBuilder


internal class RenamedRootContextBuilder<F>(wrapped: RootContextBuilder<F>, newName: String)
    : RootContextBuilder<F> by (wrapped as MinutestRootContextBuilder<F>).copy(name = newName)