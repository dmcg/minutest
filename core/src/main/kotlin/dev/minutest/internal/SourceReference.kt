package dev.minutest.internal

/**
 * Represents the place in the source where a test or context was defined
 */
data class SourceReference(val path: String, val lineNumber: Int)