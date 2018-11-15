# Minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings the power of Kotlin to JUnit 5, giving

* Spec-style nested contexts and tests
* Easy reuse of test code
* On-the fly generation of tests
* A level of expressiveness that has changed the way I write tests.

## Installation

[Instructions](installation.md)

## Usage

To just test simple functions, define your tests in a subclass of JupiterTests. The JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests) looks like this.

```kotlin
// Implement JupiterTests to run Minutests with JUnit 5
class FirstMinutests : JupiterTests {

    // tests are grouped in a context
    override val tests = context<Unit> {

        // define a test by calling test
        test("my first test") {
            // Minutest doesn't have any built-in assertions.
            // Here I'm using JUnit assertEquals
            assertEquals(2, 1 + 1)
        }

        // here is another test
        test("my second test") {
            assertNotEquals(42, 6 * 9)
        }
    }
}
```

Most tests require access to some state. The collection of state required by the tests is called the test fixture. If you are testing a class, at simplest the fixture might be an instance of the class.

```kotlin
class SimpleStackExampleTests : JupiterTests {

    // The fixture type is the generic type of the test, here Stack<String>
    override val tests = context<Stack<String>> {

        // The fixture block tells Minutest how to create an instance of the fixture.
        // Minutest will call it once for every test.
        fixture {
            Stack()
        }

        test("add an item") {
            // In a test, 'this' is the fixture created above
            assertTrue(this.isEmpty())

            this.add("item")
            assertFalse(this.isEmpty())
        }

        // another test will use a new fixture instance
        test("fixture is fresh") {
            // you can also access the fixture as 'it' if it reads nicer
            assertTrue(it.isEmpty())

            // or use the implicit 'this'
            assertFalse(isNotEmpty())
        }
    }
}
```

Minutests can be defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```kotlin
class StackExampleTests : JupiterTests {

    override val tests = context<Stack<String>> {

        // The tests in the root context run with this empty stack
        fixture {
            Stack()
        }

        test("is empty") {
            assertTrue(it.isEmpty())
        }

        test("throws EmptyStackException when popped") {
            assertThrows<EmptyStackException> { pop() }
        }

        test("throws EmptyStackException when peeked") {
            assertThrows<EmptyStackException> { peek() }
        }

        // nested a context
        context("after pushing an element") {

            // This context modifies the fixture from its parent -
            // the tests run with the single item stack.
            modifyFixture {
                parentFixture.push("one")
            }

            test("is not empty") {
                assertFalse(it.isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(it.isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(it.isEmpty())
            }
        }
    }
}
```

More complicated tests will have more than one piece of state. 

```kotlin
class FixtureExampleTests : JupiterTests {

    // We have multiple items of test state, so make a separate fixture class
    class Fixture {
        // these would be the fields of your JUnit test
        val stack1 = Stack<Int>()
        val stack2 = Stack<Int>()
    }

    // now our context type is Fixture
    override val tests = context<Fixture> {

        // create it in a fixture block
        fixture { Fixture() }

        // and access it in tests
        test("add all single item") {
            stack2.add(1)
            stack1.addAll(stack2)
            assertEquals(listOf(1), stack1.toList())
        }

        test("add all multiple items") {
            assertTrue(stack1.isEmpty() && stack2.isEmpty(), "fixture is clean each test")
            stack2.addAll(listOf(1, 2))
            stack1.addAll(stack2)
            assertEquals(listOf(1, 2), stack1.toList())
        }
    }
}
```


This runs the following tests

![StackExampleTests](images/StackExampleTests.png)

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```kotlin
class ParameterisedTests : JupiterTests {

    override val tests = context<Unit> {

        // Once we are in a context, running the same tests for multiple parameters is
        // as easy as calling `test()` for each one.
        listOf("a", "oo", "racecar", "able was I ere I saw elba").forEach { candidate ->
            test("$candidate is a palindrome") {
                assertTrue(candidate.isPalindrome())
            }
        }

        listOf("", "ab", "a man a plan a canal pananma").forEach { candidate ->
            test("$candidate is not a palindrome") {
                assertFalse(candidate.isPalindrome())
            }
        }
    }
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
```

![ParameterisedTests](images/ParameterisedTests.png)

## Reusing Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

```kotlin
// To run the same tests against different implementations, first define a function
// taking the implementation and returning a TestContext
private fun TestContext<MutableCollection<String>>.behavesAsMutableCollection(
    collectionName: String,
    factory: () -> MutableCollection<String>
) {

    fixture { factory() }

    context("$collectionName behaves as MutableCollection") {

        test("is empty") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can invoke the function to verify the contract in a context

class ArrayListTests : JupiterTests {

    override val tests = context<MutableCollection<String>> {
        behavesAsMutableCollection("ArrayList") { ArrayList() }
    }
}

// We can reuse the contract for different collections.

// Here we use the convenience InlineJupiterTests to reduce boilerplate
class LinkedListTests : InlineJupiterTests<MutableCollection<String>>({

    behavesAsMutableCollection("LinkedList") { LinkedList() }

})
```

## Generate Tests

Go crazy and unleash the `Power of Kotlin` to generate your tests on the fly.

```kotlin
// We can define functions that return tests for later injection

private typealias StringStack = Stack<String>

private fun TestContext<StringStack>.isEmpty(isEmpty: Boolean) =
    test("is " + (if (isEmpty) "" else "not ") + "empty") {
        assertEquals(isEmpty, size == 0)
        if (isEmpty)
            assertThrows<EmptyStackException> { peek() }
        else
            assertNotNull(peek())
    }

private fun TestContext<StringStack>.canPush() = test("can push") {
    val initialSize = size
    val item = "*".repeat(initialSize + 1)
    push(item)
    assertEquals(item, peek())
    assertEquals(initialSize + 1, size)
}

private fun TestContext<StringStack>.canPop() = test("can pop") {
    val initialSize = size
    val top = peek()
    assertEquals(top, pop())
    assertEquals(initialSize - 1, size)
    if (size > 0)
        assertNotEquals(top, peek())
}

private fun TestContext<StringStack>.cantPop() = test("cant pop") {
    assertThrows<EmptyStackException> { pop() }
}

// In order to give multiple sets of tests, in this example we are using JUnit @TestFactory functions
class GeneratingExampleTests {

    // JUnit will run the tests from annotated functions
    @TestFactory fun `stack tests`() = junitTests<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            // invoke the functions to create tests
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

    @TestFactory fun `multiple tests on multiple stacks`() = junitTests<StringStack> {

        fixture { StringStack() }

        // here we generate a context with 3 tests for each of 4 stacks
        (0..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                modifyFixture {
                    (1..itemCount).forEach { add(it.toString()) }
                }

                isEmpty(itemCount == 0)
                canPush()
                canPop(itemCount > 0)
            }
        }
    }
}

private fun TestContext<StringStack>.canPop(canPop: Boolean) = if (canPop) canPop() else cantPop()
```

The last of these generates the following tests

![MultipleStackExamples](images/MultipleStackExamples.png)


## Other Features

* [Immutable fixtures](immutable-fixtures.md)
* [JUnit rules](junit-rules.md)

## Support

Minutest is still feeling its way towards a humane API. Until we reach version 1 this is subject to change - we'll try not to break things but it's better to move fast. Please do let us know what is working and what isn't, either physically or conceptually.

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.