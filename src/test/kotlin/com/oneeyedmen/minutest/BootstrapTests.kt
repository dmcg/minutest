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

    @Minutest fun `Minutests are passed state`(s: State) {
        testCount++
        assertEquals(42, s.a)
    }

    @Minutest fun `Minutests can return state`(s: State): State {
        testCount++
        return s
    }

    @Minutest fun `Minutests can return a lambda`(): () -> Int = {
        testCount++
    }

    @Minutest fun `Minutests can return a function`() = ::`plain old Test annotation`

    @Minutest fun `Minutests can return a function that requires state`() = ::`Minutests are passed state`

//    @Minutest val `Minutests can be vals` = ::`Minutests are passed state`

    @AfterAll @JvmStatic fun checkTestCount() {
        assertEquals(7, testCount)
    }
}


class State {
    val a = 42
}