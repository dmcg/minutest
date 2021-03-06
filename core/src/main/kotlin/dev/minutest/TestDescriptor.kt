package dev.minutest

/**
 * The context of a test execution.
 */
interface TestDescriptor {
    val name: String
    val parent: TestDescriptor?

    fun path(): List<TestDescriptor> = generateSequence(this, TestDescriptor::parent)
        .takeWhile { it !is RootDescriptor }
        .toList()
        .reversed()

    fun fullName(): List<String> = path().map(TestDescriptor::name)

    fun pathAsString() = fullName().joinToString("/")

}