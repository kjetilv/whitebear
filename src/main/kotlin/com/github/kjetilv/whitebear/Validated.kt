@file:Suppress("unused")

package com.github.kjetilv.whitebear

interface ErrorModel<F, E> {

    val empty: E

    infix fun isEmpty(e: E): Boolean

    fun combine(e1: E, e2: E): E

    fun add(e: E, c: F): E

    infix fun str(es: E) = "$es"
}

fun <F> failureList(str: (F) -> String = { "$it" }): ErrorModel<F, List<F>> = FailureList<F>(str)

fun <E, F, R> validated(
    errorModel: ErrorModel<F, E>,
    action: ValidatorContext<E, F>.() -> R,
): R =
    action(ErrorModelValidationContext(errorModel))

sealed interface ValidatorContext<E, F> {

    fun <T> valid(value: T): Validated<T, E>

    fun <T> invalid(vararg failures: F): Validated<T, E>

    fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E, F>

    infix fun <T> Validated<T, E>.validateThat(test: (T) -> Boolean?): OrInvalidate<T, E, F>

    fun collect(vararg validated: Validated<*, E>): Validated<Any, E>

    fun <T> collectToT(vararg validated: Validated<*, E>): Validated<T, E>

    infix fun <T> Validated<T, E>.annotateInvalid(errorProvider: () -> F): Validated<T, E>

    fun <T, R> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
    ): Zipper1<T, R, E>

    fun <T, R, RR> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
    ): Zipper2<T, R, RR, E>

    fun <T, R, RR, RRR> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        v3: Validated<RRR, E>,
    ): Zipper3<T, R, RR, RRR, E>

    fun <T, R, V> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        combiner: (T, R) -> V,
    ): Validated<V, E>

    fun <T, R, RR, V> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        combiner: (T, R, RR) -> V,
    ): Validated<V, E>

    fun <T, R, RR, RRR, V> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        v3: Validated<RRR, E>,
        combiner: (T, R, RR, RRR) -> V,
    ): Validated<V, E>
}

sealed interface Validated<T, E> {

    val value: T

    val error: E

    infix fun <R> map(mapping: (T) -> R): Validated<R, E>

    infix fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E>

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

interface OrInvalidate<T, E, F> {

    infix fun elseInvalid(toErrors: (T) -> F): Validated<T, E>
}

interface Zipper1<T, R, E> {

    infix fun <RR> zipWith(validated: Validated<RR, E>) = zipWith { validated }

    infix fun <RR> zipWith(validator: () -> Validated<RR, E>): Zipper2<T, R, RR, E>

    infix fun <V> map(combiner: (T, R) -> V): Validated<V, E>

    infix fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E>

    val sum: Validated<*, E>
}

interface Zipper2<T, R, RR, E> {

    infix fun <RRR> zipWith(validated: Validated<RRR, E>) = zipWith { validated }

    infix fun <RRR> zipWith(validator: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E>

    infix fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E>

    infix fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E>

    val sum: Validated<*, E>
}

interface Zipper3<T, R, RR, RRR, E> {

    infix fun <RRRR> zipWith(validated: Validated<RRRR, E>) = zipWith { validated }

    infix fun <RRRR> zipWith(validator: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E>

    infix fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E>

    infix fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E>

    val sum: Validated<*, E>
}

interface Zipper4<T, R, RR, RRR, RRRR, E> {

    infix fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E>

    infix fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, E>): Validated<V, E>

    val sum: Validated<*, E>
}
