package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named

fun Named.andThenJust(name: String): Named = object : Named {
    override val name: String = name
    override val parent = this@andThenJust
}