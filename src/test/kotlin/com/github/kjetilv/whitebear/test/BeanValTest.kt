package com.github.kjetilv.whitebear.test

import com.github.kjetilv.whitebear.beanvalidation.JakartaValidation
import jakarta.validation.Validation
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BeanValTest {

    class X(
        @field:Max(2L) val value: Int,
        @field:NotNull val name: String? = null,
    )

    @Test
    fun test_simple() {
        val validator = Validation.buildDefaultValidatorFactory().validator

        val x1v = validator.validate(X(1, "foo"))
        assertTrue { x1v.isEmpty() }

        val x2v = validator.validate(X(3))
        assertEquals(2, x2v.size)
    }

    @Test
    fun test_integrate() {

        val jakartaValidation = JakartaValidation()

        val bad1 = jakartaValidation validateItem X(3)

        assertFalse { bad1.valid }

        val item = X(1, "sdfsdf")

        jakartaValidation(item) {
            valid(item)
        }.apply {
            assertTrue { valid }
        }

        val validated = jakartaValidation validateItem item
        validated
            .apply {
                assertTrue { valid }
            }
            .map {
                it to it.name
            }
            .apply {
                assertTrue { valid }
            }

        val simplr = jakartaValidation validateItem X(1)
        assertFalse(simplr.valid)
    }
}

