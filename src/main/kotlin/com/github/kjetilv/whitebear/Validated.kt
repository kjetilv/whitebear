@file:Suppress("unused")

package com.github.kjetilv.whitebear

interface ErrorModel<E, A> {

    val empty: A

    infix fun isEmpty(aggregator: A): Boolean

    fun combine(aggregator1: A, aggregator2: A): A

    fun add(aggregator: A, error: E): A

    infix fun str(aggregator: A) = "$aggregator"
}

fun <E> failureList(str: (E) -> String = { "$it" }): ErrorModel<E, List<E>> =
    FailureList<E>(str)

fun <E, A, R> validate(
    errorModel: ErrorModel<A, E>,
    action: ValidatorContext<E, A>.() -> R,
): R =
    action(ErrorModelValidationContext(errorModel))

sealed interface ValidatorContext<E, A> {

    fun <T> valid(value: T): Validated<T, E>

    fun <T> invalid(vararg failures: A): Validated<T, E>

    fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E, A>

    infix fun <T> Validated<T, E>.validateThat(test: (T) -> Boolean?): OrInvalidate<T, E, A>

    fun collect(vararg validated: Validated<*, E>): Validated<Any, E>

    infix fun <T> Validated<T, E>.annotateInvalid(errorProvider: () -> A): Validated<T, E>
}

sealed interface Validated<T, E> {

    val value: T

    val error: E

    infix fun <R> map(mapping: (T) -> R): Validated<R, E>

    infix fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E>

    fun collect(vararg validated: Validated<*, E>): Validated<Any, E>

    infix fun <R> zipWith(
        validated: Validated<R, E>,
    ): Zipper1<T, R, E> =
        zipWith { validated }

    fun <R, RR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
    ) =
        zipWith { validated1 } zipWith { validated2 }

    fun <R, RR, RRR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
    ) =
        zipWith { validated1 } zipWith { validated2 } zipWith { validated3 }

    fun <R, RR, RRR, RRRR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
        validated4: Validated<RRRR, E>,
    ) =
        zipWith { validated1 } zipWith { validated2 } zipWith { validated3 } zipWith { validated4 }

    infix fun <R> zipWith(validator: () -> Validated<R, E>): Zipper1<T, R, E>

    infix fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E>

    infix fun validValueOr(errorConsumer: (E) -> Nothing): T

    val valid: Boolean

    val invalid: Boolean
}

interface OrInvalidate<T, E, A> {

    infix fun elseInvalid(toErrors: (T) -> A): Validated<T, E>
}

interface Zipper1<T, R, E> {

    infix fun <RR> zipWith(validated: Validated<RR, E>) = zipWith { validated }

    infix fun <RR> zipWith(validator: () -> Validated<RR, E>): Zipper2<T, R, RR, E>

    infix fun <O> map(combiner: (T, R) -> O): Validated<O, E>

    infix fun <O> flatMap(combiner: (T, R) -> Validated<O, E>): Validated<O, E>

    val sum: Validated<*, E>
}

interface Zipper2<T, R, RR, E> {

    infix fun <RRR> zipWith(validated: Validated<RRR, E>) = zipWith { validated }

    infix fun <RRR> zipWith(validator: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E>

    infix fun <O> map(combiner: (T, R, RR) -> O): Validated<O, E>

    infix fun <O> flatMap(combiner: (T, R, RR) -> Validated<O, E>): Validated<O, E>

    val sum: Validated<*, E>
}

interface Zipper3<T, R, RR, RRR, E> {

    infix fun <RRRR> zipWith(validated: Validated<RRRR, E>) = zipWith { validated }

    infix fun <RRRR> zipWith(validator: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E>

    infix fun <O> map(combiner: (T, R, RR, RRR) -> O): Validated<O, E>

    infix fun <O> flatMap(combiner: (T, R, RR, RRR) -> Validated<O, E>): Validated<O, E>

    val sum: Validated<*, E>
}

interface Zipper4<T, R, RR, RRR, RRRR, E> {

    infix fun <O> map(combiner: (T, R, RR, RRR, RRRR) -> O): Validated<O, E>

    infix fun <O> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<O, E>): Validated<O, E>

    val sum: Validated<*, E>
}
