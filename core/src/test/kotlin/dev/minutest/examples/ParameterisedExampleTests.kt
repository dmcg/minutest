package dev.minutest.examples

import dev.minutest.experimental.checkedAgainst
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ParameterisedExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Unit>(checkedAgainst { Assertions.assertEquals(summary, it) }) {

        context("palindromes") {

            // Creating a test for each of multiple parameters is as easy as
            // calling `test()` for each one.
            listOf("a", "oo", "racecar", "able was I ere I saw elba").forEach { candidate ->
                test("$candidate is a palindrome") {
                    assertTrue(candidate.isPalindrome())
                }
            }
        }
        context("not palindromes") {
            listOf("", "ab", "a man a plan a canal pananma").forEach { candidate ->
                test("$candidate is not a palindrome") {
                    assertFalse(candidate.isPalindrome())
                }
            }
        }
    }

    val summary = listOf(
        "root",
        "  palindromes",
        "    a is a palindrome",
        "    oo is a palindrome",
        "    racecar is a palindrome",
        "    able was I ere I saw elba is a palindrome",
        "  not palindromes",
        "     is not a palindrome",
        "    ab is not a palindrome",
        "    a man a plan a canal pananma is not a palindrome"
    )
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
