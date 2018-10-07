# Minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

## Installation
You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest)

You will need to include JUnit 5 on your test classpath. If you can work out what to do based on the 
[JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) then you're ready to use Minutest.

## Usage

Minutests are defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/StackExampleTests.kt
```

This runs the following tests

![StackExampleTests](docs/images/StackExampleTests.png)


The key difference between Minutest and XUnit tests is the location of the test fixture - the thing being tested and the supporting cast. In XUnit the fixture is the fields of the test class, with tests being defined in special methods of that class. Minutest separates the tests, which are defined by calling the `test(name)` method, from the fixture, which is usually a separate class. 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/FixtureExampleTests.kt
```

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/ParameterisedTests.kt
```

![ParameterisedTests](docs/images/ParameterisedTests.png)

More complicated scenarios can be approached by writing your own function that returns a test or a context.

## Reusing Tests
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/SubclassExampleTests.kt
```

## Generate Tests

Go crazy and unleash the `Power of Kotlin` to generate your tests on the fly.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/GeneratingExampleTests.kt
```

The last of these generates the following tests

![MultipleStackExamples](docs/images/MultipleStackExamples.png)

## Immutable Fixtures

Are you a functional programmer slumming it with Kotlin? Minutest allows immutable fixtures.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/Minutest/examples/ImmutableExampleTests.kt
```

## JUnit Rules

Power JUnit 4 user? Minutest supports JUnit 4 TestRules. As far as I can tell, it does it better than JUnit 5!

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/Minutest/examples/JUnitRulesExampleTests.kt
```
