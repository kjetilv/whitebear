package com.github.kjetilv.whitebear

import com.github.kjetilv.whitebear.impl.ErrorModelValidationContext

fun <E, A, R> validate(errorProcessor: ErrorProcessor<E, A>, action: ValidationContext<E, A>.() -> R): R =
    validator(errorProcessor) validate action

internal fun <E, A> validator(errorProcessor: ErrorProcessor<E, A>): Validator<E, A> =
    DefaultValidator(errorProcessor)

fun <E> errorList(str: (List<E>) -> String = { "$it" }) =
    errorProcessor<E, List<E>>(
        empty = emptyList(),
        isEmpty = List<E>::isEmpty,
        str = str,
        wrap = ::listOf,
    )

/**
 * A {@link SimpleErrorModel}
 */
fun <E> simpleErrorList(str: (List<E>) -> String = { "$it" }) =
    simpleErrors(
        empty = emptyList(),
        str = str,
        combine = List<E>::plus
    )

fun <E> simpleErrors(
    empty: E,
    isEmpty: (E) -> Boolean = { it == empty },
    str: (E) -> String = { "$it" },
    combine: (E, E) -> E,
): SimpleErrorProcessor<E> = object : SimpleErrorProcessor<E> {

    override val empty: E = empty

    override fun isEmpty(aggregator: E) =
        isEmpty(aggregator)

    override fun str(aggregator: E) =
        str(aggregator)

    override fun combine(aggregator1: E, aggregator2: E): E =
        combine(aggregator1, aggregator2)
}

fun <E, A> errorProcessor(
    empty: A,
    isEmpty: (A) -> Boolean = { it == empty },
    str: (A) -> String = { "$it" },
    wrap: (E) -> A,
): ErrorProcessor<E, A> = object : ErrorProcessor<E, A> {

    override val empty: A = empty

    override fun isEmpty(aggregator: A) = isEmpty(aggregator)

    override fun wrap(error: E): A = wrap(error)

    override fun str(aggregator: A) = str(aggregator)

    override fun combine(aggregator1: A, aggregator2: A): A = combine(aggregator1, aggregator2)
}

internal interface Validator<E, A> {

    infix fun <R> validate(action: ValidationContext<E, A>.() -> R): R
}

internal class DefaultValidator<E, A>(errorProcessor: ErrorProcessor<E, A>) : Validator<E, A> {

    private val errorModelValidationContext = ErrorModelValidationContext(errorProcessor)

    override fun <R> validate(action: ValidationContext<E, A>.() -> R): R =
        action(errorModelValidationContext)
}
