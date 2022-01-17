package com.github.kjetilv.whitebear

import com.github.kjetilv.whitebear.impl.ErrorModelValidationContext

fun <E, A, R> validate(errorModel: ErrorModel<E, A>, action: ValidationContext<E, A>.() -> R): R =
    action(ErrorModelValidationContext(errorModel))

fun <E> failureList(str: (E) -> String = { "$it" }): ErrorModel<E, List<E>> =
    failureModel(emptyList(), List<E>::plus) { l, e -> l + e }

fun <E> simpleFailureList(str: (E) -> String = { "$it" }): SimpleErrorModel<List<E>> =
    simpleFailureModel(emptyList(), List<E>::plus)

fun <E> simpleFailureModel(empty: E, combiner: (E, E) -> E): SimpleErrorModel<E> =
    object : SimpleErrorModel<E> {

        override val empty: E = empty

        override fun combine(aggregator1: E, aggregator2: E): E = combiner(aggregator1, aggregator2)
    }

fun <E, A> failureModel(empty: A, combiner: (A, A) -> A, adder: (A, E) -> A): ErrorModel<E, A> =
    object : ErrorModel<E, A> {

        override val empty: A = empty

        override fun add(aggregator: A, error: E): A = adder(aggregator, error)

        override fun combine(aggregator1: A, aggregator2: A): A = combiner(aggregator1, aggregator2)
    }
