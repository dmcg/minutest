# minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

## Installation
Life is too short to jump through the hoops to Maven Central, but you can pick up builds on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest)

## Usage

minutest See [ExampleTests](src/test/kotlin/com/oneeyedmen/minutest/ExampleTests.kt), viz:

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/ExampleTests.kt
```

## More Advanced Use

The key to minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

So if you want to reuse the same test for different concrete implementations, define the test with a function and call it for subclasses.

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/SubclassExampleTests.kt
```

Unleash the power of Kotlin to generate your tests on the fly.

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/GeneratingExampleTests.kt
```
