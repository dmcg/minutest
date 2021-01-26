[Minutest](README.md)

# JUnit Rules

Are you a power JUnit 4 user? Minutest supports JUnit 4 TestRules. As far as I can tell, it does it better than JUnit 5!

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/experimental/JUnitRulesExampleTests.kt>
```kotlin
class JUnitRulesExampleTests : JUnit5Minutests {

    class Fixture {
        // make rules part of the fixture, no need for an annotation
        val testFolder = TemporaryFolder()
    }

    fun tests() = rootContext<Fixture> {

        given { Fixture() }

        // tell the context to use the rule for each test in it and its children
        applyRule(this@JUnitRulesExampleTests::class.java.name) { this.testFolder }

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/experimental/JUnitRulesExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/experimental/JUnitRulesExampleTests.kt)</small>

[end-insert]: <>