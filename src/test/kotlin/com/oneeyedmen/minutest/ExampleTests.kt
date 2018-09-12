package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

data class Result(val value: Int) {
    constructor(): this(0)

    fun add(i: Int) = Result(value + i)
}

object ExampleTests : Minutests {

    @Test fun `plain old Test annotation`() {}

    @Minutest fun `defaults to 0`(result: Result): Result {
        Assertions.assertEquals(0, result.value)
        return result
    }

    @Minutest fun `adds another value`(result: Result) {
        Assertions.assertEquals(2, `defaults to 0`(result).add(2).value)
    }
}


