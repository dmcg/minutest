# minutest

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

See [ExampleTests](src/test/kotlin/com/oneeyedmen/minutest/ExampleTests.kt)

```insert-kotlin src/test/kotlin/com/oneeyedmen/minutest/ExampleTests.kt
```

And then move on to more interesting [GeneratingExampleTests](src/test/kotlin/com/oneeyedmen/minutest/GeneratingExampleTests.kt)

I'm not yet publishing to Maven Central, but you can pick up builds on BinTray using something like

```
repositories {
    ...
    maven {
        url = uri("https://dl.bintray.com/dmcg/oneeyedmen-mvn/")
    }
}

dependencies {
    ...
    testCompile("com.oneeyedmen:minutest:0.3.0")
}
```
