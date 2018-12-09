# [Minutest](README.md)

## Installation

I don't think that Minutest is ready for Android or KotlinJS or KotlinNative projects yet, sorry. If you prove me wrong please let me know.

You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest). So you need to reference JCenter as a repository.

```groovy
repositories {
    jcenter()
}
```

You will need to include Minutest and JUnit 5 on your test compilation classpath, and the JUnit engine on your test runtime classpath. 

```groovy
testCompile "org.junit.jupiter:junit-jupiter-api:+"
testCompile "com.oneeyedmen:minutest:+"

testRuntime "org.junit.jupiter:junit-jupiter-engine:+"
testRuntime "org.junit.platform:junit-platform-launcher:+"
```

Finally you need to let test tasks know to use JUnit 5

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

My apologies to the Mavenites. If you are one then please try to work out what to do based on the [JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) and then submit a PR for this readme!

