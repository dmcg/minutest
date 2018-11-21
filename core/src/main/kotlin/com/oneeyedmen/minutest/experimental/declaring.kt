package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.RuntimeNode
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext

class TopLevelContextBuilder(val builderFn: (String) -> RuntimeNode) {
    fun build(name: String): RuntimeNode {
        return builderFn(name)
    }
}

inline fun <reified F> context(noinline builder: Context<Unit, F>.() -> Unit): TopLevelContextBuilder =
    TopLevelContextBuilder { n -> topLevelContext(n, asKType<F>(), builder) }
