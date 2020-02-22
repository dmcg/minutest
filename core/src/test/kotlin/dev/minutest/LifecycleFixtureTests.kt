package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class LifecycleFixtureTests : JUnit5Minutests {

    val log = mutableListOf<String>()

    @AfterEach
    fun checkLog() {
        assertEquals(listOf(
            "in builder",
            "in dependency builder",
            "in factory",
            "in test",
            "in dependency closer"
        ), log)
    }

    class Fixture(val dependency: OutputStream)

    fun tests() = rootContext<Fixture> {

        lifecycleFixture(
            dependencyBuilder = {
                log += "in dependency builder"
                ByteArrayOutputStream()
            },
            dependencyCloser = { os, _ ->
                log += "in dependency closer"
                os.close()
            },
            factory = { os, _ ->
                log += "in factory"
                Fixture(os)
            }
        )

        test("test") {
            log += "in test"
            dependency.write(42)
        }


        log += "in builder"
    }
}