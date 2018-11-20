package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Focusable
import com.oneeyedmen.minutest.internal.ContextBuilder

fun Context<*, *>.just(builder: () -> Focusable) {
    (this as ContextBuilder<*, *>).hasFocused = true
    builder().isFocused = true
}