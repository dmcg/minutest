# minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

## Installation
You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest)

You will need to include JUnit 5 on your test classpath. If you can work out what to do based on the 
[JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) then you're probably worthy to use minutest.

## Usage

minutest can be used to define tests in a nested Spec style, with contexts and tests inside those contexts. 

```kotlin
package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


object ExampleTests {

    // In the simplest case, make the fixture the thing that you are testing
    @TestFactory fun `stack is our fixture`() = junitTests<Stack<String>> {

        // define the fixture for enclosed scopes
        fixture { Stack() }

        context("an empty stack") {

            test("is empty") {
                assertEquals(0, size) // note that the fixture is 'this'
                assertThrows<EmptyStackException> { peek() }
            }

            test("can have an item pushed") {
                push("one")
                assertEquals("one", peek())
                assertEquals(1, size)
            }
        }

        context("a stack with one item") {

            // we can modify the outer fixture
            modifyFixture { push("one") }

            test("is not empty") {
                assertEquals(1, size)
                assertEquals("one", peek())
            }

            test("removes and returns item on pop") {
                assertEquals("one", pop())
                assertEquals(0, size)
            }
        }
    }

    // If you have more state, make a separate fixture class
    class Fixture {
        val stack1 = Stack<String>()
        val stack2 = Stack<String>()
    }

    // and then use it in your tests
    @TestFactory fun `separate fixture class`() = junitTests<Fixture> {

        fixture { Fixture() }

        context("stacks with no items") {
            test("error to try to swap") {
                assertThrows<EmptyStackException> {
                    stack1.swapTop(stack2)
                }
            }
        }

        context("stacks with items") {
            modifyFixture {
                stack1.push("on 1")
                stack2.push("on 2")
            }

            test("swap top items") {
                stack1.swapTop(stack2)
                assertEquals("on 2", stack1.peek())
                assertEquals("on 1", stack2.peek())
            }
        }
    }

    // You can modify the fixture before, and inspect it after
    @TestFactory fun `before and after`() = junitTests<Fixture> {
        fixture { Fixture() }

        before {
            stack1.push("on 1")
        }

        before {
            stack2.push("on 2")
        }

        after {
            println("in after")
            assertTrue(stack1.isEmpty())
        }

        test("before was called") {
            assertEquals("on 1", stack1.peek())
            assertEquals("on 2", stack2.peek())
            stack1.pop()
        }
    }
}

private fun <E> Stack<E>.swapTop(otherStack: Stack<E>) {
    val myTop = pop()
    push(otherStack.pop())
    otherStack.push(myTop)
}
```

## More Advanced Use

The key to minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

So if you want to reuse the same test for different concrete implementations, define the test with a function and call it for subclasses.

```kotlin
package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import java.util.*


// To run the same tests against different implementations, first define a function taking the implementation and
// returning a TestContext
fun TestContext<MutableCollection<String>>.behavesAsMutableCollection(
    collectionName: String,
    factory: () -> MutableCollection<String>
) {
    context("$collectionName behaves as MutableCollection") {

        fixture { factory() }

        test("is empty") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can invoke the function to define a context to be run

object ArrayListTests {
    @TestFactory fun tests() = junitTests<MutableCollection<String>> {
        behavesAsMutableCollection("ArrayList") { ArrayList() }
    }
}

object LinkedListTests{
    @TestFactory fun tests() = junitTests<MutableCollection<String>> {
        behavesAsMutableCollection("LinkedList") { LinkedList() }
    }
}
```

Unleash the `Power of Kotlin` to generate your tests on the fly.

```kotlin
package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


private typealias StringStack = Stack<String>

object GeneratingExampleTests {

    // We can define functions that return tests for later injection

    private fun TestContext<StringStack>.isEmpty(isEmpty: Boolean) =
        test("is " + (if (isEmpty) "" else "not ") + "empty") {
            assertEquals(isEmpty, size == 0)
            if (isEmpty)
                assertThrows<EmptyStackException> { peek() }
            else
                assertNotNull(peek())
        }

    private fun TestContext<StringStack>.canPush() =
        test("can push") {
            val initialSize = size
            val item = "*".repeat(initialSize + 1)
            push(item)
            assertEquals(item, peek())
            assertEquals(initialSize + 1, size)
        }

    private fun TestContext<StringStack>.canPop() =
        test("can pop") {
            val initialSize = size
            val top = peek()
            assertEquals(top, pop())
            assertEquals(initialSize - 1, size)
            if (size > 0)
                assertNotEquals(top, peek())
        }

    private fun TestContext<StringStack>.cantPop() =
        test("cant pop") {
            assertThrows<EmptyStackException> { pop() }
        }

    @TestFactory fun `invoke functions to inject tests`() = junitTests<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            isEmpty(true)
            canPush()
            cantPop()
        }

        context("a stack with one item") {
            modifyFixture { push("one") }

            isEmpty(false)
            canPush()
            canPop()

            test("has the item on top") {
                assertEquals("one", peek())
            }
        }
    }

    @TestFactory fun `generate contexts to test with multiple values`() = junitTests<StringStack> {

        fun TestContext<StringStack>.canPop(canPop: Boolean) = if (canPop) canPop() else cantPop()

        (0..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                fixture {
                    StringStack().apply {
                        (1..itemCount).forEach { add(it.toString()) }
                    }
                }

                isEmpty(itemCount == 0)
                canPush()
                canPop(itemCount > 0)
            }
        }
    }
}
```

Are you a died-in-the-wool functional programmer? If so, what are you doing slumming it with Kotlin? But at least minutest allows immutable fixtures.

```kotlin
package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object ImmutableExampleTests {

    // If you like this FP stuff, you may want to test an immutable fixture.

    @TestFactory fun `immutable fixture`() = junitTests<List<String>> {
        fixture { emptyList() }

        // test_ allows you to return the fixture
        test_("add an item and return the fixture") {
            val newList = this + "item"
            assertEquals("item", newList.first())
            newList
        }

        // which will be available for inspection in after
        after {
            println("in after")
            assertEquals("item", first())
        }

        // there are also before_ and after_ which return new fixtures
    }
}
```

Power JUnit 4 user? minutest supports JUnit 4 TestRules. As far as I can tell, it does it better than JUnit 5!

```kotlin
package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.applyRule
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import org.junit.rules.TemporaryFolder


object JunitRulesExampleTests {

    class Fixture {
        // make rules part of the fixture
        val testFolder = TemporaryFolder()
    }

    @TestFactory fun `temporary folder rule`() = junitTests<Fixture>() {

        fixture { Fixture() }

        // tell the context to use the rule
        applyRule(Fixture::testFolder)

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
```