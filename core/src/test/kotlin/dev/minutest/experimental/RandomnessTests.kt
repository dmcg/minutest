package dev.minutest.experimental

import dev.minutest.rootContext
import dev.minutest.testing.runTests
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RandomnessTests {

    @Test
    fun reuses_random_seed_until_test_passes() {
        val randomValues = mutableListOf<Int>()

        fun executeTests(shouldPass: Boolean) = runTests( // *
            rootContext {
                randomTest("the test") { random, _ ->
                    randomValues += random.nextInt()
                    assertTrue(shouldPass)
                }
            }
        )

        // uses the same random values all the time the test is failing
        executeTests(false)
        executeTests(false)
        assertEquals(randomValues[0], randomValues[1])

        // until it passes
        executeTests(true)
        assertEquals(randomValues[1], randomValues[2])

        // when it then uses different values
        executeTests(true)
        assertNotEquals(randomValues[2], randomValues[3])
    }

    // * - we have to re-evaluate the root context for each test because it the test tree is built (indirectly)
    // by executeTests and mutates state.
}
