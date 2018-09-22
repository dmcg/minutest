# minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

## Installation
You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest)

You will need to include JUnit 5 on your test classpath. If you can work out what to do based on the 
[JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) then you're probably worthy to use minutest.

## Usage

minutest can be used to define tests in a nested Spec style, with contexts and tests inside those contexts. 

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/examples/ExampleTests.kt
```

## More Advanced Use

The key to minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

So if you want to reuse the same test for different concrete implementations, define the test with a function and call it for subclasses.

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/examples/SubclassExampleTests.kt
```

Unleash the `Power of Kotlin` to generate your tests on the fly.

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/examples/GeneratingExampleTests.kt
```

Are you a died-in-the-wool functional programmer? If so, what are you doing slumming it with Kotlin? But at least minutest allows immutable fixtures.

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/examples/ImmutableExampleTests.kt
```

Power JUnit 4 user? minutest supports JUnit 4 TestRules. As far as I can tell, it does it better than JUnit 5!

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/examples/JUnitRulesExampleTests.kt
```
