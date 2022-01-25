package beanval

import beanval.Beans.violations
import com.github.kjetilv.whitebear.SimpleErrorModel
import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.ValidationContext
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

typealias BeansContext = ValidationContext<List<ConstraintViolation<*>>, List<ConstraintViolation<*>>>

typealias BeanErrors = SimpleErrorModel<List<ConstraintViolation<*>>>

object Beans {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private val multiViolations: BeanErrors = simpleFailureList()

    infix fun <R> BeansContext.violations(r: R): List<ConstraintViolation<R>>? =
        validator.validate(r).toList().takeIf { it.isNotEmpty() }

    operator fun <R> invoke(action: BeansContext.() -> R): R =
        validate(multiViolations) {
            action(this)
        }

    infix fun <T> validate(item: T) : Validated<T, List<ConstraintViolation<*>>> =
        Beans {
            valid(item) withViolations {
                violations(it)
            }
        }
}

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

        Beans {
            valid(X(3)) withViolations {
                violations(it)
            }
        }

        val bad = Beans {
            valid(X(3)) withViolations {
                violations(it)
            }
        }

        assertFalse { bad.valid }

        Beans {
            valid(X(1, "sdfsdf")) withViolations {
                violations(it)
            } map {
                it to it.name
            }
        }.apply {
            assertTrue { valid }
        }

        val simplr = Beans validate X(1)
        assertFalse(simplr.valid)
    }
}

