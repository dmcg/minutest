package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.executeTests
import com.oneeyedmen.minutest.rootContext
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RandomnessTests {
    @Test
    fun reuses_random_seed_until_test_passes() {
        var testIsPassing = true
        val randomValues = mutableListOf<Int>()
        
        val tests = rootContext<Unit> {
            randomTest("the test") { random, _ ->
                randomValues += random.nextInt()
                assertTrue(testIsPassing)
            }
        }
        
        testIsPassing = false
        executeTests(tests)
        executeTests(tests)
        
        testIsPassing = true
        executeTests(tests)
        executeTests(tests)
        
        assertEquals(randomValues[0],randomValues[1])
        
        assertNotEquals(randomValues[2],randomValues[3])
    }
}
