package com.github.kjetilv.whitebear

fun <E, R> validate(errorProcessor: ErrorProcessor<E>, action: ValidationContext<E>.() -> R): R =
    validator(errorProcessor) validate action

internal fun <E> validator(errorProcessor: ErrorProcessor<E>): Validator<E> =
    DefaultValidator(errorProcessor)

fun <E> errorList() =
    errorProcessor<List<E>>(
        empty = emptyList(),
        combine = List<E>::plus,
        isEmpty = List<E>::isEmpty,
    )

fun <E> errors(
    empty: E,
    combine: (E, E) -> E,
): ErrorProcessor<E> = object : ErrorProcessor<E> {

    override val empty: E = empty

    override fun combine(error1: E, error2: E): E =
        combine(error1, error2)
}

fun <E> errorProcessor(
    empty: E,
    combine: (E, E) -> E,
    isEmpty: (E) -> Boolean = { it == empty },
): ErrorProcessor<E> = object : ErrorProcessor<E> {

    override val empty: E = empty

    override fun combine(error1: E, error2: E): E = combine(error1, error2)
}

internal interface Validator<E> {

    infix fun <R> validate(action: ValidationContext<E>.() -> R): R
}

internal class DefaultValidator<E>(errorProcessor: ErrorProcessor<E>) : Validator<E> {

    private val errorProcessorValidationContext = errorProcessorValidationContext(errorProcessor)

    override fun <R> validate(action: ValidationContext<E>.() -> R): R =
        action(errorProcessorValidationContext)
}
