package com.oneeyedmen.minutest

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KProperty1


fun <T, R: TestRule> TestContext<T>.applyRule(property: KProperty1<T, R>) {
    addTransform { test ->
        MinuTest(test.name) {
            this.also { fixture ->
                val statement = object : Statement() {
                    override fun evaluate() {
                        test(this@MinuTest)
                    }
                }
                property.get(fixture).apply(statement, Description.createTestDescription("GENERATED TEST", name))
            }
        }
    }
}