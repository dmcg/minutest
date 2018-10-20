package com.oneeyedmen.minutest

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [RandomWordController::class])
@AutoConfigureMockMvc
class SpringTests {

  @Autowired
  lateinit var mvc: MockMvc

  @MockBean
  lateinit var service: RandomWordService

  @TestFactory
  fun springContextTest() = junitTests<List<String>> {
    context("a spring integration test environment") {
      fixture {
        listOf("catflap", "rubberplant", "marzipan")
      }

      before {
        whenever(service.words(any<Int>())) doAnswer {
          val n = it.arguments.first() as Int
          slice(0 until n)
        }
      }

      after { reset(service) }

      test("we can access an autowired bean") {
        assertEquals(this, service.words(3))
      }

      test("we can hit a spring web endpoint") {
        mvc
          .perform(get("/words").accept(TEXT_PLAIN))
          .andExpect(status().isOk)
          .andExpect(content().string(joinToString()))
      }
    }
  }
}

interface RandomWordService {
  fun words(n: Int = 1): Iterable<String>
}

@RestController
open class RandomWordController(
  @Autowired private val service: RandomWordService
) {
  @RequestMapping("/words", method = [GET], produces = [TEXT_PLAIN_VALUE])
  fun words(): String = service.words(3).joinToString()
}
