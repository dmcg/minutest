[Minutest](README.md)

# Installation

If you were previously using `com.oneeyedmen.minutest` I've screwed up - please read [how to migrate](com.oneeyedmen.md) 

I don't think that Minutest is ready for Android or KotlinJS or KotlinNative projects yet, sorry. If you prove me wrong please let me know.

You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest). So you need to reference JCenter as a repository.

```groovy
repositories {
    jcenter()
}
```

You will need to include Minutest and JUnit 5 on your test compilation classpath, and the JUnit engine on your test runtime classpath. 

```groovy
dependencies {
    ...
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.3.2"
    testImplementation "dev.minutest:minutest:+"
    testRuntime "org.junit.jupiter:junit-jupiter-engine:5.3.2"
    testRuntime "org.junit.platform:junit-platform-launcher:1.3.2"
}
```

`dev.minutest:minutest:+` will build against the latest version of minutest - if you want more stability use a particular version label instead - eg `dev.minutest:minutest:1.0.0`

You now you need to let test tasks know to use JUnit 5

```groovy
tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events "skipped", "failed"
        }
    }
}
```

This setup will allow you to use JUnit 5 [Assertions](https://junit.org/junit5/docs/current/user-guide/#writing-tests-assertions).
If you want to use the kotlin.test assertions you will need to include them in your dependencies as well

```groovy
dependencies {
    ...
    testImplementation "org.jetbrains.kotlin:kotlin-test"
    testImplementation "org.jetbrains.kotlin:kotlin-test-junit"
}
```

My apologies to the Mavenites. If you are one then please try to work out what to do based on the [JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) and then submit a PR for this readme!

