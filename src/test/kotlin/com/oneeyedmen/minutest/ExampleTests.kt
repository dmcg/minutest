package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.LList.Cons
import com.oneeyedmen.minutest.LList.Nil
import org.junit.jupiter.api.Assertions.assertEquals


sealed class LList<out T> {
    object Nil : LList<Nothing>()
    data class Cons<T>(val car: T, val cdr: LList<T> = Nil) : LList<T>()
}

fun LList<*>.size(): Int = when (this) {
    Nil -> 0
    is Cons -> 1 + cdr.size()
}

fun <T> LList<T>.prepend(item: T): LList<T> = Cons(item, this)

object ExampleTests : Minutests {

    @Minutest fun `empty list`(): List<NamedFunction> {
        val emptyList = Nil
        return listOf(
            "has size 0" {
                assertEquals(0, emptyList.size())
            },
            "is Nil" {
                assertEquals(Nil, emptyList)
            },
            "can be added to" {
                assertEquals(Cons("fred"), emptyList.prepend("fred"))
            }
        )
    }
}


