package dev.minutest.experimental

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.synchronized
import dev.minutest.test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import java.lang.Thread.sleep
import java.util.concurrent.ForkJoinPool


class ParallelExecutionTests : JUnit5Minutests {

    val serialTestsRun = mutableListOf<Int>().synchronized()
    val parallelTestsRun = mutableListOf<Int>().synchronized()

    fun tests() = rootContext {

        executor(ForkJoinPool.commonPool())

        (1..10).forEach { index ->
            test("test $index") {
                sleep((Math.random() * 100).toLong())
                parallelTestsRun.add(index)
            }
        }

        context("switch off parallel") {
            executor(null)
            (1..10).forEach { index ->
                test("test $index") {
                    sleep((Math.random() * 100).toLong())
                    serialTestsRun.add(index)
                }
            }
        }
    }


    @AfterEach
    fun check() {
        assertNotEquals(parallelTestsRun.sorted(), parallelTestsRun, "Not parallel")
        assertEquals(serialTestsRun.sorted(), serialTestsRun, "Not serial")
    }
}

