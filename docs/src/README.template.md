# Minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest.dev/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings the power of Kotlin to JUnit, giving

* Spec-style nested contexts and tests
* Easy reuse of test code
* On-the fly generation of tests
* A level of expressiveness that should change the way you write tests.

## Installation

[Instructions](installation.md)

If you were previously using `com.oneeyedmen.minutest` I've screwed up - please read [how to migrate](com.oneeyedmen.md) 

## Usage

To just test simple functions, define your tests in class which mixes-in JUnit5Minutests. The JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests) looks like this.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/FirstMinutests.kt
```

Most tests require access to some state. The collection of state required by the tests is called the test fixture. If you are testing a class, at simplest the fixture might be an instance of the class.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/SimpleStackExampleTests.kt
```

Minutests can be defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/StackExampleTests.kt
```

This runs the following tests

![StackExampleTests](images/StackExampleTests.png)

Tests for cooperating components will typically have one piece of state. In this case make the fixture hold all the state. 

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