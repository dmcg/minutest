[![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest.dev/images/download.svg)](https://bintray.com/dmcg/oneeyedmen-mvn/minutest.dev/_latestVersion)
[![Build Status](https://travis-ci.org/dmcg/minutest.svg?branch=master)](https://travis-ci.org/dmcg/minutest)

# Minutest

JUnit multiplied by Kotlin

## Why Another Test Framework?

JUnit is great for quickly writing and running tests as part of a TDD workflow, but try to do anything unusual and you have to reach for the documentation and specially written annotations. 

Minutest extends JUnit with a simple model that allows you to solve your own problems with plain Kotlin.

For example

### Conditionally running a test

JUnit has a special annotation

```kotlin
@Test
@EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server")
fun onlyOnStagingServer() {
    // ...
}
```

Minutest is just Kotlin

```kotlin
if (getenv("ENV") == "staging-server" ) test("only on staging server") {
    // ...
}
```

### Parameterised tests

JUnit has three annotations

```kotlin
@DisplayName("Fruit tests")
@ParameterizedTest(name = "{index} ==> fruit=''{0}'', rank={1}")
@CsvSource("apple, 1", "banana, 2", "'lemon, lime', 3")
fun testWithCustomDisplayNames(fruit: String, rank, String) {
    // ...
}
```

Minutest is just Kotlin

```kotlin
context("Fruit tests") {
    listOf("apple" to 1, "banana" to 2, "lemon, lime" to 3).forEachIndexed { index, (fruit, rank) ->
        test("$index ==> fruit='$fruit', rank=$rank") {
            // ...
        }
    }
}
```

### Nested Tests

JUnit needs more annotations

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/StackExampleTestsJUnit.kt>
```kotlin
@DisplayName("A stack")
class TestingAStackDemo {

    var stack: Stack<Any> = Stack()

    @Nested
    @DisplayName("when new")
    inner class WhenNew {

        @Test
        fun `is empty`() {
            assertTrue(stack.isEmpty())
        }
    }

    @Nested
    @DisplayName("after pushing an element")
    inner class AfterPushing {

        var anElement = "an element"

        @BeforeEach
        fun pushAnElement() {
            stack.push(anElement)
        }

        @Test
        fun `it is no longer empty`() {
            assertFalse(stack.isEmpty())
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/StackExampleTestsJUnit.kt](../core/src/test/kotlin/dev/minutest/examples/StackExampleTestsJUnit.kt)</small>

[end-insert]: <>

Minutest is just Kotlin

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt>
```kotlin
class StackExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Stack<Any>> {

        given { Stack() }

        context("when new") {

            test("is empty") {
                assertTrue(it.isEmpty())
            }
        }

        context("after pushing an element") {

            beforeEach {
                it.push("an element")
            }

            test("it is no longer empty") {
                assertFalse(it.isEmpty())
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt)</small>

[end-insert]: <>


Minutest brings the power of Kotlin to JUnit, providing

* A clean DSL to define nested contexts and tests
* Generation and manipulation of tests at runtime 
* Much easier reuse of test code

For more information on how why Minutest is like it is, see [My New Test Model](http://oneeyedmen.com/my-new-test-model.html) .

## Installation

[Instructions](installation.md)

## Moving from JUnit to Minutest

Here is a version of the JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests), converted to Kotlin.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/MyFirstJUnitJupiterTests.kt>
```kotlin
class MyFirstJUnitJupiterTests {

    private val calculator = Calculator()

    @Test
    fun addition() {
        calculator.add(2)
        assertEquals(2, calculator.currentValue)
    }

    @Test
    fun subtraction() {
        calculator.subtract(2)
        assertEquals(-2, calculator.currentValue)
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/MyFirstJUnitJupiterTests.kt](../core/src/test/kotlin/dev/minutest/examples/MyFirstJUnitJupiterTests.kt)</small>

[end-insert]: <>

In Minutest it looks like this

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/MyFirstMinutests.kt>
```kotlin
// Mix-in JUnit5Minutests to run Minutests with JUnit 5
//
// (JUnit 4 support is also available, see [JUnit4Minutests].)
class MyFirstMinutests : JUnit5Minutests {

    // tests are grouped in a context
    fun tests() = rootContext<Calculator> {

        // We need to tell Minutest how to build the fixture
        given { Calculator() }

        // define a test with a test block
        test("addition") {
            // inside tests, the fixture is `it`
            it.add(2)
            assertEquals(2, it.currentValue)
        }

        // each new test gets its own new fixture
        test("subtraction") { calculator ->
            subtract(2)
            assertEquals(-2, calculator.currentValue)
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/MyFirstMinutests.kt](../core/src/test/kotlin/dev/minutest/examples/MyFirstMinutests.kt)</small>

[end-insert]: <>

Most tests require access to some state. The collection of state required by the tests is called the test fixture. In JUnit we use the fields of the test class as the fixture - in this case just the calculator. JUnit uses a fresh instance of the test class for each test method run, which is why the state of calculator after `addition` does not affect the result of `subtraction`.

Minutest does not create a fresh instance of the test class for each test, instead it invokes a `fixture` block in a context and passes the result into tests as `this`.

Tests for cooperating components will typically have more state than just the thing we are testing. In this case make the fixture hold all the state. 

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt>
```kotlin
class ControlPanel(
    private val beep: () -> Unit,
    private val launchRocket: () -> Unit
) {
    private var keyTurned: Boolean = false

    fun turnKey() {
        keyTurned = true
    }

    fun pressButton() {
        if (keyTurned)
            launchRocket()
        else
            beep()
    }
    val warningLightOn get() = keyTurned
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    // The fixture consists of all the state affected by tests
    class Fixture {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        given { Fixture() }

        context("key not turned") {
            test("light is off") {
                assertFalse(controlPanel.warningLightOn)
            }
            test("cannot launch when pressing button") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(launched)
            }
        }

        context("key turned") {
            beforeEach {
                controlPanel.turnKey()
            }
            test("light is on") {
                assertTrue(controlPanel.warningLightOn)
            }
            test("launches when pressing button") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(launched)
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt)</small>

[end-insert]: <>

Understanding fixtures is key to Minutest - [read more](fixtures.md)

## Run Code to Make Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt>
```kotlin
class ParameterisedExampleTests : JUnit5Minutests {

    fun tests() = rootContext {

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
            "▾ tests",
            "  ▾ palindromes",
            "    ✓ a is a palindrome",
            "    ✓ oo is a palindrome",
            "    ✓ racecar is a palindrome",
            "    ✓ able was I ere I saw elba is a palindrome",
            "  ▾ not palindromes",
            "    ✓  is not a palindrome",
            "    ✓ ab is not a palindrome",
            "    ✓ a man a plan a canal pananma is not a palindrome"
        )
    }
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
```
<small>[../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt)</small>

[end-insert]: <>

## Reuse Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt>
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
        given {
            ArrayList()
        }

        behavesAsMutableCollection()
    }
}

// We can reuse the contract for different collections.
class LinkedListTests : JUnit5Minutests {

    fun tests() = rootContext<MutableCollection<String>> {
        given {
            LinkedList()
        }

        behavesAsMutableCollection()
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt)</small>

[end-insert]: <>


## Structure Tests

When your tests grow so that they need more structure, Minutest has extensions to support Given When Then blocks
 
[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/scenarios/ScenariosExampleTests.kt>
```kotlin
class ControlPanel(
    private val beep: () -> Unit,
    private val launchRocket: () -> Unit
) {
    private var keyTurned: Boolean = false

    fun turnKey() {
        keyTurned = true
    }

    fun pressButton(): Boolean =
        when {
            keyTurned -> {
                launchRocket()
                true
            }
            else -> {
                beep()
                false
            }
        }

    val warningLightOn get() = keyTurned
}

class ScenariosExampleTests : JUnit5Minutests {

    class Fixture {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {

        // Scenario defines a nested context
        Scenario("Cannot launch without key switch") {

            // GivenFixture sets up the fixture for the scenario
            GivenFixture("key not turned") {
                Fixture()
            }.Then("warning light is off") {
                // Then can check the setup
                assertFalse(controlPanel.warningLightOn)
            }

            // When performs the operation
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was false") { result ->
                // Then has access to the result
                assertFalse(result)
            }.And("it beeped") {
                // And makes another Thens with checks
                assertTrue(beeped)
            }.And("rocket was not launched") {
                assertFalse(launched)
            }
        }

        Scenario("Will launch with key switch") {
            GivenFixture("key turned") {
                Fixture().apply {
                    controlPanel.turnKey()
                }
            }.Then("warning light is on") {
                assertTrue(controlPanel.warningLightOn)
            }

            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was true") { result ->
                assertTrue(result)
            }.And("it didn't beep") {
                assertFalse(beeped)
            }.And("rocket was launched") {
                assertTrue(launched)
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/scenarios/ScenariosExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/scenarios/ScenariosExampleTests.kt)</small>

[end-insert]: <>

## Other Features

The [Cookbook](Cookbook.md) shows other ways to use Minutest. 

## Evolution

We're pretty happy with the core Minutest language and expect not to make any breaking changes without a major version update. Features like JUnit 4 support and test annotations are public but experimental - if you use anything in an `experimental` package you should expect it to change between minor releases, and move completely once adopted into the stable core.

Note that we aim for source and not binary compatibility. Some implementation may move from  methods to extension functions, or from constructors to top level or companion-object functions.

## Support

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.