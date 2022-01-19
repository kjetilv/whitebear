package com.github.kjetilv.whitebear

import com.github.kjetilv.whitebear.impl.ErrorModelValidationContext

fun <E, A, R> validate(errorModel: ErrorModel<E, A>, action: ValidationContext<E, A>.() -> R): R =
    action(ErrorModelValidationContext(errorModel))

fun <E> failureList(str: (List<E>) -> String = { "$it" }): ErrorModel<E, List<E>> =
    failureModel(emptyList(), str = str, combine = List<E>::plus) { l, e -> l + e }

fun <E> simpleFailureList(str: (List<E>) -> String = { "$it" }): SimpleErrorModel<List<E>> =
    simpleFailureModel(emptyList(), str = str, combine = List<E>::plus)

fun <E> simpleFailureModel(
    empty: E,
    isEmpty: (E) -> Boolean = { it == empty },
    str: (E) -> String = { "$it" },
    combine: (E, E) -> E,
): SimpleErrorModel<E> =
    object : SimpleErrorModel<E> {

        override val empty: E = empty

        override fun isEmpty(aggregator: E) = isEmpty(aggregator)

        override fun str(aggregator: E) = str(aggregator)

        override fun combine(aggregator1: E, aggregator2: E): E = combine(aggregator1, aggregator2)
    }

fun <E, A> failureModel(
    empty: A,
    isEmpty: (A) -> Boolean = { it == empty },
    str: (A) -> String = { "$it" },
    combine: (A, A) -> A,
    add: (A, E) -> A,
): ErrorModel<E, A> =
    object : ErrorModel<E, A> {

        override val empty: A = empty

        override fun isEmpty(aggregator: A) = isEmpty(aggregator)

        override fun str(aggregator: A) = str(aggregator)

        override fun add(aggregator: A, error: E): A = add(aggregator, error)

        override fun combine(aggregator1: A, aggregator2: A): A = combine(aggregator1, aggregator2)
    }
