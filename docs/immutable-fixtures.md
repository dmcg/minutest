# [Minutest](README.md)

## Immutable Fixtures

Are you a functional programmer slumming it with Kotlin? Minutest allows immutable fixtures.

```kotlin
class ImmutableExampleTests : JupiterTests {

    // If you like this FP stuff, you may want to test an immutable fixture.
    override val tests = context<List<String>> {

        // List<String> is immutable
        fixture { emptyList() }

        // test_ allows you to return the fixture
        test_("add an item and return the fixture") {
            val newList = this + "item"
            assertEquals("item", newList.first())
            newList
        }

        // which will be available for inspection in after
        after {
            assertEquals("item", first())
        }
    }
}
```
