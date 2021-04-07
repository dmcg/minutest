package dev.minutest.examples.experimental

import dev.minutest.experimental.executor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.synchronized
import dev.minutest.test
import org.junit.AfterClass
import org.junit.Assert.assertNotEquals
import java.lang.Thread.sleep
import java.util.concurrent.ForkJoinPool


class ParallelExecutionExampleTests : JUnit5Minutests {

    val testsRun = mutableListOf<Int>().synchronized()

    fun tests() = rootContext("parallel tests") {

        // if you set an executor, it will be used to run the tests
        executor(ForkJoinPool.commonPool())

        (1..10).forEach { index ->
            test("test $index") {
                sleep((Math.random() * 100).toLong())
                testsRun.add(index)
            }
        }
    }

    @AfterClass
    fun check() {
        assertNotEquals(testsRun.sorted(), testsRun)
    }
}



