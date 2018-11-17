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

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/FirstMinutests.kt
```

Most tests require access to some state. The collection of state required by the tests is called the test fixture. If you are testing a class, at simplest the fixture might be an instance of the class.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/SimpleStackExampleTests.kt
```

Minutests can be defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/StackExampleTests.kt
```

More complicated tests will have more than one piece of state. 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/FixtureExampleTests.kt
```

This runs the following tests

![StackExampleTests](images/StackExampleTests.png)

[More discussion about fixtures](fixtures.md)

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/ParameterisedTests.kt
```

![ParameterisedTests](images/ParameterisedTests.png)

## Reusing Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/ContractsExampleTests.kt
```

## Generate Tests

Go crazy and unleash the `Power of Kotlin` to generate your tests on the fly.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/GeneratingExampleTests.kt
```

The last of these generates the following tests

![MultipleStackExamples](images/MultipleStackExamples.png)


## Other Features

* [JUnit rules](junit-rules.md)

## Support

Minutest is still feeling its way towards a humane API. Until we reach version 1 this is subject to change - we'll try not to break things but it's better to move fast. Please do let us know what is working and what isn't, either physically or conceptually.

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.