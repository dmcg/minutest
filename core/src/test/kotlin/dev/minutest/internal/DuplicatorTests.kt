package dev.minutest.internal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DuplicatorTests {

    interface Base {
        val string: String
    }
    data class Thing(
        val int: Int,
        override val string: String
    ) : Base

    @Test
    fun `duplicatorFor duplicates a data class given a property`() {
        val duplicator = duplicatorFor(Thing::string)

        val thing = Thing(42, "banana")
        Assertions.assertEquals(
            Thing(42, "kumquat"),
            duplicator(thing, "kumquat")
        )
    }
}