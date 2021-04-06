package dev.minutest.experimental

import dev.minutest.combinedWith
import dev.minutest.internal.duplicatorFor
import kotlin.reflect.KProperty1

typealias Mutation<T> = (T) -> T

fun <T> mutation(name: String, mutation: Mutation<T>) = object : Mutation<T> by mutation {
    override fun toString() = name
}

fun <T, V> mutation(name: String, value: V, mutation: Mutation<T>) =
    mutation("$name = $value", mutation)

infix fun <T> Mutation<T>.andThen(another: Mutation<T>): Mutation<T> =
    mutation("($this) and ($another)") {
        another(this(it))
    }

fun <T> Iterable<Mutation<T>>.combined(
    other: Iterable<Mutation<T>>
): List<Mutation<T>> = combinedWith(other).map { (a, b) -> a andThen b }

fun <T : Any, V> mutation(
    property: KProperty1<T, V>,
    value: V
): Mutation<T> {
    // outside the lambda so that we don't keep doing the reflective lookups
    val duplicator = duplicatorFor(property)
    return mutation(property.name, value) {
        duplicator(it, value)
    }
}

fun <T : Any, V> Iterable<V>.asMutationsOf(
    property: KProperty1<T, V>
) = map { mutation(property, it) }


/**
 * Something that has been mutated - the [name] says how.
 */
data class Mutant<T>(
    val name: String,
    val value: T
)