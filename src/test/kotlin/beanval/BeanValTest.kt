package beanval

import com.github.kjetilv.whitebear.failureList
import com.github.kjetilv.whitebear.simpleFailureList
import com.github.kjetilv.whitebear.validate
import jakarta.validation.ConstraintViolation
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

        val validator = Validation.buildDefaultValidatorFactory().validator

        val constrationViolations: (X) -> List<ConstraintViolation<*>>? = { validator.validate(it).toList().takeIf { it.isNotEmpty() } }
        val constrationViolation: (X) -> ConstraintViolation<*>? = { constrationViolations(it)?.firstOrNull() }

        val multiViolations = simpleFailureList<ConstraintViolation<*>>()

        val singleViolation = failureList<ConstraintViolation<*>>()

        val bad = validate(multiViolations) {
            valid(X(3)) withViolations {
                constrationViolations(it)
            }
        }

        assertFalse { bad.valid }

        validate(singleViolation) {
            valid(X(3)) withViolation {
                constrationViolation(it)
            }
        }.apply {
            assertFalse { valid }
        }

        validate(multiViolations) {
            valid(X(1, "sdfsdf")) withViolations {
                constrationViolations(it)
            } map {
                it to it
            }
        }.apply {
            assertTrue { valid }
        }
    }
}

