[![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest.dev/images/download.svg)](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)
[![Build Status](https://travis-ci.org/dmcg/minutest.svg?branch=master)](https://travis-ci.org/dmcg/minutest)

# Minutest

JUnit multiplied by Kotlin

## Why Another Test Framework?

JUnit is great for quickly writing and running tests as part of a TDD workflow, but try to do anything unusual and you have to reach for the documentation and specially written annotations. 

Minutest extends JUnit with a simple model that allows you to solve your own problems - it's just Kotlin.

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

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/StackExampleTestsJUnit.kt
```

Minutest is just Kotlin

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt
```


Minutest brings the power of Kotlin to JUnit, providing

* A clean DSL to define nested contexts and tests
* Generation and manipulation of tests at runtime 
* Much easier reuse of test code

For more information on how why Minutest is like it is, see [My New Test Model](http://oneeyedmen.com/my-new-test-model.html) .

## Installation

[Instructions](installation.md)

## Moving from JUnit to Minutest

Here is a version of the JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests), converted to Kotlin.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/MyFirstJUnitJupiterTests.kt
```

In Minutest it looks like this

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/MyFirstMinutests.kt
```

Most tests require access to some state. The collection of state required by the tests is called the test fixture. In JUnit we use the fields of the test class as the fixture - in this case just the calculator. JUnit uses a fresh instance of the test class for each test method run, which is why the state of calculator after `addition` does not affect the result of `subtraction`.

Minutest does not create a fresh test class for each test, instead it invokes a `fixture` block in a context and passes the result into tests as `this`.

Tests for cooperating components will typically have more state than just the thing we are testing. In this case make the fixture hold all the state. 

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/CompoundFixtureExampleTests.kt
```

Understanding fixtures is key to Minutest - [read more](fixtures.md)

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt
```

## Reusing Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt
```

## Other Features

The [Cookbook](Cookbook.md) shows other ways to use Minutest. 

## Evolution

We're pretty happy with the core Minutest language and expect not to make any breaking changes without a major version update. Features like JUnit 4 support and test annotations are public but experimental - if you use anything in an `experimental` package you should expect it to change between minor releases, and move completely once adopted into the stable core.

Note that we aim for source and not binary compatibility. Some implementation may move from  methods to extension functions, or from constructors to top level or companion-object functions.

## Support

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.