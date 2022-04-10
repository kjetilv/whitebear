package com.github.kjetilv.whitebear

import com.github.kjetilv.whitebear.impl.ErrorModelValidationContext

fun <E, A, R> validate(errors: Errors<E, A>, action: ValidationContext<E, A>.() -> R): R =
    validator(errors).validate(action)

fun <E, A> validator(errors: Errors<E, A>): Validator<E, A> =
    DefaultValidator(errors)

fun <E> failureList(
    str: (List<E>) -> String = { "$it" },
): Errors<E, List<E>> =
    failureModel(emptyList(), str = str, combine = List<E>::plus) { l, e -> l + e }

/**
 * A {@link SimpleErrorModel}
 */
fun <E> simpleFailureList(str: (List<E>) -> String = { "$it" }) =
    simpleFailureModel(emptyList(), str = str, combine = List<E>::plus)

fun <E> simpleFailureModel(
    empty: E,
    isEmpty: (E) -> Boolean = { it == empty },
    str: (E) -> String = { "$it" },
    combine: (E, E) -> E,
): SimpleErrors<E> = object : SimpleErrors<E> {

    override val empty: E = empty

    override fun isEmpty(aggregator: E) =
        isEmpty(aggregator)

    override fun str(aggregator: E) =
        str(aggregator)

    override fun combine(aggregator1: E, aggregator2: E): E =
        combine(aggregator1, aggregator2)
}

fun <E, A> failureModel(
    empty: A,
    isEmpty: (A) -> Boolean = { it == empty },
    str: (A) -> String = { "$it" },
    combine: (A, A) -> A,
    add: (A, E) -> A,
): Errors<E, A> = object : Errors<E, A> {

    override val empty: A = empty

    override fun isEmpty(aggregator: A) = isEmpty(aggregator)

    override fun str(aggregator: A) = str(aggregator)

    override fun add(aggregator: A, error: E): A = add(aggregator, error)

    override fun combine(aggregator1: A, aggregator2: A): A = combine(aggregator1, aggregator2)
}

interface Validator<E, A> {

    infix fun <R> validate(action: ValidationContext<E, A>.() -> R): R
}

internal class DefaultValidator<E, A>(errors: Errors<E, A>) : Validator<E, A> {

    private val errorModelValidationContext = ErrorModelValidationContext(errors)

    override fun <R> validate(action: ValidationContext<E, A>.() -> R): R =
        action(errorModelValidationContext)
}
