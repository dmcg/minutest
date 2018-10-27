//package com.oneeyedmen.minutest
//
//import com.oneeyedmen.minutest.internal.MinuTest
//import com.oneeyedmen.minutest.junit.junitTests
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.fail
//import org.junit.jupiter.api.DynamicTest
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestFactory
//import org.junit.jupiter.api.assertThrows
//import kotlin.streams.asSequence
//
//
//object TransformTests {
//
//    @TestFactory fun `before and after`() = junitTests<MutableList<String>> {
//        fixture { mutableListOf() }
//
//        // befores in order
//        before {
//            add("before")
//        }
//
//        before {
//            add("before too")
//        }
//
//        after {
//            assertEquals(listOf("before", "before too", "during"), this)
//            add("after")
//        }
//
//        after {
//            assertEquals(listOf("before", "before too", "during", "after"), this)
//        }
//
//        test("before has been called") {
//            assertEquals(listOf("before", "before too"), this)
//            add("during")
//        }
//
//        context("also applies to sub-contexts") {
//            test("before is called") {
//                assertEquals(listOf("before", "before too"), this)
//                add("during")
//            }
//        }
//    }
//
//    @Test fun `afters run even on exception`() {
//        val list = mutableListOf<String>()
//
//        val tests = junitTests<Unit> {
//            after {
//                list.add("after")
//            }
//            test("I fail") {
//                throw Throwable("banana")
//            }
//        }.asSequence()
//
//        assertThrows<Throwable> {
//            ((tests.first() as DynamicTest)).executable.execute()
//        }
//
//        assertEquals(listOf("after"), list)
//    }
//
//    @TestFactory fun `test transform`() = junitTests<Unit> {
//
//        addTransform { test ->
//            MinuTest(test.name) {}
//        }
//
//        test("transform can ignore test") {
//            fail("Shouldn't get here")
//        }
//    }
//}