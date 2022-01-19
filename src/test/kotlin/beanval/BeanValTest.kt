package beanval

import com.github.kjetilv.whitebear.Validated
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

        val validate: (X) -> List<ConstraintViolation<*>> = { validator.validate(it).toList() }

        val errorModel = simpleFailureList<ConstraintViolation<*>>()

        val bad: Validated<X, List<ConstraintViolation<*>>> = validate(errorModel) {
            valid(X(3)) applyViolations {
                validate(it).takeIf { it.isNotEmpty() }
            }
        }

        assertFalse { bad.valid }

        val good = validate(errorModel) {
            valid(X(1, "sdfsdf")) applyViolations {
                validate(it).takeIf {
                    it.isNotEmpty()
                }
            } map {
                it to it
            }
        }

        assertTrue { good.valid }
    }
}

