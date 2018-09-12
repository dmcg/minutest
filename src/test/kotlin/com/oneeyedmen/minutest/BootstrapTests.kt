package com.oneeyedmen.minutest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


object BootstrapTests : Minutests {

    private var testCount = 0

    @Test fun `plain old Test annotation`() {
        testCount++
    }

    @Minutest fun `Minutest annotation is discovered as dynamic test`() {
        testCount++
    }

    @Minutest fun `will be passed state`(s: State) {
        testCount++
        assertEquals(42, s.a)
    }

    @Minutest fun `can return state`(s: State): State {
        testCount++
        return s
    }

    @Minutest fun `will invoke a returned lambda`(): () -> Int = {
        testCount++
    }

    @Minutest fun `will invoke a returned function`() = ::`plain old Test annotation`

    @Minutest fun `will invoke a returned function that requires state`() = ::`will be passed state`

    @Minutest fun `will invoke a list list of method references`() =
        listOf(::`plain old Test annotation`, ::`will be passed state`)

    @Minutest fun `will invoke a sequence of method references`() =
        sequenceOf(::`plain old Test annotation`, ::`will be passed state`)

    @Minutest fun `will invoke a list of named lambdas`() = listOf(
        NamedFunction("do a thing") { testCount ++ },
        NamedFunction("do another thing") { testCount ++ }
    )

    @Minutest fun `will invoke a sequence of named lambdas`() = sequenceOf(
        NamedFunction("do a thing") { testCount ++ },
        NamedFunction("do another thing") { testCount ++ }
    )


    @AfterAll @JvmStatic fun checkTestCount() {
        assertEquals(15, testCount)
    }
}


class State {
    val a = 42
}