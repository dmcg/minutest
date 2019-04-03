[Minutest](README.md)

# Focus and Skip [Experimental]

Once you have run some tests, you can point to them in IntelliJ's test output pane and re-run individual tests. But sometimes you want to be able to signal in source that you only want to run some tests, or that one or more should be skipped.

Enter Focus and Skip.

```kotlin
class SkipAndFocusExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {

        // Apply the FOCUS annotation to a test
        FOCUS - test("this test is focused, only other focused things will be run") {}

        context("not focused, so won't be run") {
            test("would fail if the context was run") {
                fail("should not have run")
            }
        }

        context("contains a focused thing, so is run") {

            test("isn't focused, so doesn't run") {
                fail("should not have run")
            }

            FOCUS - context("focused, so will be run") {

                test("this runs") {}

                // apply the SKIP annotation to not run whatever
                SKIP - test("skip overrides the focus") {
                    fail("should not have run")
                }

                SKIP - context("also applies to context") {
                    test("will not be run") {
                        fail("should not have run")
                    }
                }
            }
        }
    }
}
```
<small>\[[core/src/test/kotlin/dev/minutest/examples/experimental/SkipAndFocusExampleTests.kt
](../core/src/test/kotlin/dev/minutest/examples/experimental/SkipAndFocusExampleTests.kt
)\]</small>

To be honest, it's a bit arbitrary at the moment, but I find it useful, so you might too. Just don't rely on your intuition about how skip and focus interact if your tests do anything irreversible.