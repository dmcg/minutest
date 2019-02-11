package dev.minutest

import org.junit.platform.commons.annotation.Testable

/**
 * A marker that a class, method or function defines some tests.
 *
 * Not actually used by Minutest, but (should) prevent IntelliJ from reporting them as unused.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@Testable
annotation class Tests