package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.TestDescriptor

internal data class TestDescriptorHolder(var testDescriptor: TestDescriptor?) : Named {

    override val name: String
        get() = testDescriptor?.name ?: error("no testDescription set")

    override val parent: Named?
        get() = if (testDescriptor == null) error("no testDescription set") else testDescriptor?.parent
}