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
    fun test_context() {

        val jakartaValidation= JakartaValidation()

        val validated= jakartaValidation validate X(3)
    }

    @Test
    fun test_integrate() {

        val jakartaValidation = JakartaValidation()

        val bad1 = jakartaValidation validate X(3)

        assertFalse { bad1.valid }

        val item = X(1, "sdfsdf")

        assertTrue { (jakartaValidation validate item).valid }

        (jakartaValidation validate item)
            .apply {
                assertTrue { valid }
            }
            .map {
                it to it.name
            }
            .apply {
                assertTrue { valid }
            }

        val simplr = jakartaValidation validate X(1)
        assertFalse(simplr.valid)
    }
}

