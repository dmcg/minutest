package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.buildRootNode
import com.oneeyedmen.minutest.internal.askType
import com.oneeyedmen.minutest.internal.topLevelContext

class TopLevelContextBuilder(private val createTopLevelNode: (String) -> RuntimeNode) {
    fun build(name: String): RuntimeNode {
        return createTopLevelNode(name)
    }
}

inline fun <reified F> context(noinline builder: Context<Unit, F>.() -> Unit) = TopLevelContextBuilder { name ->
    topLevelContext(name, askType<F>(), builder).buildRootNode()
}
