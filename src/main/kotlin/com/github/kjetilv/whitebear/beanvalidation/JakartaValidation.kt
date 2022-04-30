package com.github.kjetilv.whitebear.beanvalidation

import com.github.kjetilv.whitebear.ErrorProcessor
import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.ValidationContext
import com.github.kjetilv.whitebear.Validator
import com.github.kjetilv.whitebear.errorList
import com.github.kjetilv.whitebear.errorProcessorValidationContext
import com.github.kjetilv.whitebear.validate
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation as JakartaBeansValidation
import jakarta.validation.Validator as JakartaBeansValidator

class JakartaValidation(
    private val jakartaBeansValidator: JakartaBeansValidator = defaultJakartaValidator,
) : Validator<List<ValidatedBean<*>>> {

    companion object {

        private val defaultJakartaValidator: JakartaBeansValidator =
            JakartaBeansValidation.buildDefaultValidatorFactory().validator
    }

    operator fun <T> invoke(action: ValidatedBeansContext.() -> Validated<T, List<ValidatedBean<T>>>) =
        validate(multiViolations, action)

    override fun <R> validate(action: ValidationContext<List<ValidatedBean<*>>>.() -> R): R =
        action(validationContext)

    infix fun <T> validate(item: T): Validated<T, List<ValidatedBean<*>>> =
        validate(multiViolations) {
            valid(item) withViolations {
                violations(it)
            }
        }

    private val multiViolations: ErrorProcessor<List<ValidatedBean<*>>> = errorList()

    private val validationContext: ValidationContext<List<ValidatedBean<*>>> =
        errorProcessorValidationContext(multiViolations)

    private infix fun <R> ValidatedBeansContext.violations(r: R): List<ValidatedBean<R>> =
        jakartaBeansValidator.validate(r)
            .toList()
            .let { violations ->
                if (violations.isEmpty()) emptyList()
                else listOf(ValidatedBean(r, violations))
            }
}

typealias ValidatedBeansContext = ValidationContext<List<ValidatedBean<*>>>

data class ValidatedBean<T>(val bean: T, val violations: List<ConstraintViolation<T>>)

