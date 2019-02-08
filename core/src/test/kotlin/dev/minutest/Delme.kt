package dev.minutest

import dev.minutest.junit.JUnit4Minutests
import kotlin.test.assertEquals


class PartionedTests : JUnit4Minutests() {

    data class Fixture(val list: List<Int>, val predicates: List<(Int) -> Boolean> = emptyList()) {
        fun assertResult(expected: List<List<Int>>) {
            assertEquals(
                expected,
                list.partitioned(*predicates.toTypedArray()))
        }
    }

    fun tests() = rootContext<Fixture> {

        context("empty list") {
            fixture { Fixture(emptyList()) }

            test("returns empty result") {
                assertResult(emptyList())
            }
        }

        context("list with items") {

            fixture { Fixture(listOf(-1, 0, 1, 2, 3)) }

            context("no predicates") {
                test("returns empty list") {
                    assertEquals(
                        emptyList(),
                        list.partitioned())
                }
            }

            context("all predicates are satisfied") {
                deriveFixture { fixture.copy(predicates = listOf(::isNegative, ::isZero, ::isPositive)) }
                test("returns matches in order") {
                    assertResult(listOf(listOf(-1), listOf(0), listOf(1, 2, 3)))
                }
            }

            context("a predicate doesn't match an item") {
                deriveFixture { fixture.copy(predicates = listOf(::neverMatch, ::isNegative, ::isZero, ::isPositive)) }
                test("returns an empty list for that predicate") {
                    assertResult(listOf(emptyList(), listOf(-1), listOf(0), listOf(1, 2, 3)))
                }
            }

            context("an item doesn't match a predicate") {
                deriveFixture { fixture.copy(predicates = listOf(::isZero, ::isPositive)) }
                test("discards the item") {
                    assertResult(listOf(listOf(0), listOf(1, 2, 3)))
                }
            }
        }
    }
}

fun isNegative(x: Int) = x < 0
fun isPositive(x: Int) = x > 0
fun isZero(x: Int) = x == 0
fun neverMatch(x: Int) = false

fun <T> Iterable<T>.partitioned(vararg predicates: (T) -> Boolean): List<List<T>> =
    predicates.map { groupBy { predicates.asIterable().firstMatch(it) }.getOrDefault(it, emptyList()) }

private fun <T> Iterable<(T) -> Boolean>.firstMatch(item: T): ((T) -> Boolean)? = this.find { it(item) }