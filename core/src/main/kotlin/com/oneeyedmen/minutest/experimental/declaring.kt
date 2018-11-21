package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext


class TopLevelContextBuilder(val builder: Context<Unit, Unit>.() -> Unit) {
    fun build(name: String) = topLevelContext(name, asKType<Unit>(), builder)
}

fun context(builder: Context<Unit, Unit>.() -> Unit) = TopLevelContextBuilder(builder)

