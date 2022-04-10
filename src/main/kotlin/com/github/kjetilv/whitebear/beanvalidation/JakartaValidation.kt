package com.github.kjetilv.whitebear.beanvalidation

import com.github.kjetilv.whitebear.SimpleErrors
import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.ValidationContext
import com.github.kjetilv.whitebear.Validator
import com.github.kjetilv.whitebear.impl.ErrorModelValidationContext
import com.github.kjetilv.whitebear.simpleFailureList
import com.github.kjetilv.whitebear.validate
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation as JakartaValidation
import jakarta.validation.Validator as JakartaValidator

class JakartaValidation(
    private val jakartaBeanValidator: JakartaValidator = defaultJakartaValidator,
) : Validator<List<ValidatedBean<*>>, List<ValidatedBean<*>>> {

    companion object {

        private val defaultJakartaValidator: JakartaValidator = JakartaValidation.buildDefaultValidatorFactory().validator
    }

    private val multiViolations: SimpleErrors<List<ValidatedBean<*>>> =
        simpleFailureList()

    private val errorModelValidationContext: ValidationContext<List<ValidatedBean<*>>, List<ValidatedBean<*>>> =
        ErrorModelValidationContext(multiViolations)

    operator fun <T, R> invoke(item: T, action: ValidatedBeansContext.(Validated<T, List<ValidatedBean<*>>>) -> R): R =
        validate(multiViolations) {
            action(this, validateItem(item))
        }

    infix fun <T> validateItem(item: T): Validated<T, List<ValidatedBean<*>>> =
        validate(multiViolations) {
            valid(item) withViolations {
                violations(it)
            }
        }

    override fun <R> validate(action: ValidationContext<List<ValidatedBean<*>>, List<ValidatedBean<*>>>.() -> R): R =
        action(errorModelValidationContext)

    private infix fun <R> ValidatedBeansContext.violations(r: R): List<ValidatedBean<R>> =
        jakartaBeanValidator.validate(r).toList().let { violations ->
            if (violations.isEmpty())
                emptyList()
            else
                listOf(ValidatedBean(r, violations))
        }
}

typealias ValidatedBeansContext = ValidationContext<List<ValidatedBean<*>>, List<ValidatedBean<*>>>

data class ValidatedBean<T>(val bean: T, val violations: List<ConstraintViolation<T>>)

