package com.oneeyedmen.minutest

/**
 * Something that is given a name, possibly within some named context.
 */
interface Named {
    val name: String
    val parent: Named?
}

fun Named.fullName() =
    generateSequence(this, Named::parent)
        .map(Named::name)
        .filterNot(String::isEmpty)
        .toList()
        .reversed()