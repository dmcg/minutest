package samples.assumptions

import org.junit.Assert

fun uk.org.minutest.ContextBuilder<Unit>.assumptionsContract(assumer: (Boolean) -> Unit) = context("assumptions skip tests") {

    context("a context with assume in a fixture block") {
        modifyFixture {
            assumer("black".toLowerCase() == "white")
        }
        test("should not be run") {
            Assert.fail("shouldn't get here")
        }
    }

    test("assume in a test aborts it") {
        assumer("black".toLowerCase() == "white")
        Assert.fail("shouldn't get here")
    }
}