# [Minutest](README.md)

## Focus and Skip [Experimental]

Once you have run some tests, you can point to them in IntelliJ's test output pane and re-run individual tests. But sometimes you want to be able to signal in source that you only want to run some tests, or that one or more should be skipped.

Enter Focus and Skip.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/experimental/SkipAndFocusExampleTests.kt
```

To be honest, it's a bit arbitrary at the moment, but I find it useful, so you might too. Just don't rely on your intuition about how skip and focus interact if your tests do anything irreversible.