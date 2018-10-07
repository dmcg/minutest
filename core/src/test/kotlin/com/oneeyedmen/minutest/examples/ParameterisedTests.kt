package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory

// Running the same tests for multiple parameters is as easy as calling `test()` for each one.
object ParameterisedTests {

    // Here we don't bother with a fixture, hence <Unit>
    @TestFactory fun palindromeTests() = junitTests<Unit> {

        listOf("a", "oo", "racecar", "radar", "able was I ere I saw elba").forEach { candidate ->
            test("$candidate is a palindrome") {
                assertTrue(candidate.isPalindrome());
            }
        }

        listOf("", "ab", "a man a plan a canal suez").forEach { candidate ->
            test("$candidate is not a palindrome") {
                assertFalse(candidate.isPalindrome());
            }
        }
    }
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
