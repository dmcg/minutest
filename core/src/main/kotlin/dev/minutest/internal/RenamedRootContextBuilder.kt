package dev.minutest.internal

import dev.minutest.RootContextBuilder


internal class RenamedRootContextBuilder(wrapped: RootContextBuilder, newName: String)
    : RootContextBuilder by (wrapped as MinutestRootContextBuilder<*>).copy(name = newName)