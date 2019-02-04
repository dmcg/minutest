package uk.org.minutest.experimental

import org.opentest4j.TestAbortedException

/**
 * JUnit 5 doesn't seem to allow [org.opentest4j.TestSkippedException] thrown by @TestFactory, so we use this instead.
 */
class MinutestSkippedException : TestAbortedException()