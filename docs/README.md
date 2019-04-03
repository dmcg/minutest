# Minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest.dev/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings the power of Kotlin to JUnit, giving

* Spec-style nested contexts and tests
* Easy reuse of test code
* On-the fly generation of tests
* A level of expressiveness that should change the way you write tests.

[Why do we think a new test library is needed?](http://oneeyedmen.com/my-new-test-model.html) 

## Installation

[Instructions](installation.md)

## Usage

To just test simple functions, define your tests in a class which mixes-in JUnit5Minutests. The JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests) looks like this.

```kotlin
// Mix-in JUnit5Minutests to run Minutests with JUnit 5
class FirstMinutests : JUnit5Minutests {

    // tests are grouped in a context
    fun tests() = rootContext<Unit> {

        // define a test by calling test
        test("my first test") {
            // Minutest doesn't have any built-in assertions.
            // Here I'm using JUnit assertEquals
            assertEquals(2, 1 + 1)
        }

        // here is another one
        test("my second test") {
            assertNotEquals(42, 6 * 9)
        }
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/FirstMinutests.kt
](../core/src/test/kotlin/dev/minutest/examples/FirstMinutests.kt
)\]</small>

Most tests require access to some state. The collection of state required by the tests is called the test fixture. If you are testing a class, at simplest the fixture might be an instance of the class.

```kotlin
class SimpleStackExampleTests : JUnit5Minutests {

    // The fixture type is the generic type of the test, here Stack<String>
    fun tests() = rootContext<Stack<String>> {

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
            // you can also access the fixture as 'fixture' if it reads nicer
            assertTrue(fixture.isEmpty())

            // or use the implicit 'this'
            assertFalse(isNotEmpty())
        }
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/SimpleStackExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/SimpleStackExampleTests.kt
)\]</small>

Minutests can be defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```kotlin
class StackExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Stack<String>>("when new") {

        // The tests in the root context run with this empty stack
        fixture {
            Stack()
        }

        test("is empty") {
            assertTrue(fixture.isEmpty())
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
                assertFalse(fixture.isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(fixture.isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(fixture.isEmpty())
            }
        }
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt
)\]</small>

This runs the following tests

![StackExampleTests](images/StackExampleTests.png)

Tests for cooperating components will typically have one piece of state. In this case make the fixture hold all the state. 

```kotlin
class ControlPanel(
    val keySwitch: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton() {
        if (keySwitch())
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keySwitch()
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var keySwitchOn = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            keySwitch = { keySwitchOn },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        fixture { Fixture() }

        context("key not turned") {
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("key turned") {
            modifyFixture {
                keySwitchOn = true
            }
            test("light on") {
                assertTrue(controlPanel.warningLight)
            }
            test("will launch") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(missileLaunched)
            }
        }
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/CompoundFixtureExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/CompoundFixtureExampleTests.kt
)\]</small>

Understanding fixtures is key to Minutest - [read more](fixtures.md)

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```kotlin
class ParameterisedExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {

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

        // Minutest will check that the following tests are run
        willRun(
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
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt
)\]</small>

## Reusing Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

```kotlin
// To run the same tests against different implementations, first define a ContextBuilder extension function
// that defines the tests you want run.
fun ContextBuilder<MutableCollection<String>>.behavesAsMutableCollection() {

    context("behaves as MutableCollection") {

        test("is empty when created") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can supply the fixture and invoke the function to create the tests to verify the contract.
class ArrayListTests : JUnit5Minutests {

    fun tests() = rootContext<MutableCollection<String>> {
        fixture {
            ArrayList()
        }

        behavesAsMutableCollection()
    }
}

// We can reuse the contract for different collections.
class LinkedListTests : JUnit5Minutests {

    fun tests() = rootContext<MutableCollection<String>> {
        fixture {
            LinkedList()
        }

        behavesAsMutableCollection()
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt
)\]</small>

## Other Features

The [Cookbook](Cookbook.md) shows other ways to use Minutest. 

## Evolution

We're pretty happy with the core Minutest language and expect not to make any breaking changes without a major version update. Features like JUnit 4 support and test annotations are public but experimental - if you use anything in an `experimental` package you should expect it to change between minor releases, and move completely once adopted into the stable core.

Note that we aim for source and not binary compatibility. Some implementation may move from  methods to extension functions, or from constructors to top level or companion-object functions.

## Support

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.