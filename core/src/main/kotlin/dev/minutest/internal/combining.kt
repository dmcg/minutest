package dev.minutest

internal fun <A, B> Iterable<A>.combinedWith(
    another: Iterable<B>
): List<Pair<A, B>> =
    flatMap { first -> another.map { second -> first to second } }