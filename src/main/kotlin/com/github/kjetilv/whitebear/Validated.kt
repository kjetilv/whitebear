package com.github.kjetilv.whitebear

interface ValidationContext<E> {

    fun <T> valid(value: T): Validated<T, E>

    fun <T> valid(value: () -> T): Validated<T, E>

    fun <T> invalid(failures: E): Validated<T, E>

    fun <T> invalid(failures: () -> E): Validated<T, E>

    fun <T> validIf(value: T, test: (T) -> Boolean?): OrInvalidate<T, E>

    infix fun <T> Validated<T, E>.validIf(test: (T) -> Boolean?): OrInvalidate<T, E>

    infix fun <T> Validated<T, E>.withViolations(violations: (T) -> E?): Validated<T, E>

    infix fun <T> Validated<T, E>.annotateInvalidated(errorProvider: () -> E): Validated<T, E>

    fun collect(vararg validated: Validated<*, E>): Validated<Any, E>

    fun <T, R> zip(
        validated0: Validated<T, E>,
        validated1: Validated<R, E>,
    ): Zipper1<T, R, E> =
        validated0 zipWith validated1

    fun <T, R, RR> zip(
        validated0: Validated<T, E>,
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
    ): Zipper2<T, R, RR, E> =
        validated0.zipWith(validated1, validated2)

    fun <T, R, RR, RRR> zip(
        validated0: Validated<T, E>,
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
    ): Zipper3<T, R, RR, RRR, E> =
        validated0.zipWith(validated1, validated2, validated3)

    fun <T, R, RR, RRR, RRRR> zip(
        validated0: Validated<T, E>,
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
        validated4: Validated<RRRR, E>,
    ): Zipper4<T, R, RR, RRR, RRRR, E> =
        validated0.zipWith(validated1, validated2, validated3, validated4)
}

sealed interface Validated<T, E> {

    val valid: Boolean

    val invalid: Boolean

    infix fun <R> map(mapping: (T) -> R): Validated<R, E>

    infix fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E>

    infix fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E>

    infix fun valueOr(errorConsumer: (E) -> Nothing): T

    val valueOrNull: T?

    infix fun <R> zipWith(validated1: Validated<R, E>): Zipper1<T, R, E> =
        this zipWith { validated1 }

    infix fun <R> zipWith(validator1: () -> Validated<R, E>): Zipper1<T, R, E>

    fun <R, RR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
    ) =
        this zipWith {
            validated1
        } zipWith {
            validated2
        }

    fun <R, RR, RRR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
    ) =
        this zipWith {
            validated1
        } zipWith {
            validated2
        } zipWith {
            validated3
        }

    fun <R, RR, RRR, RRRR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>,
        validated3: Validated<RRR, E>,
        validated4: Validated<RRRR, E>,
    ) =
        this zipWith {
            validated1
        } zipWith {
            validated2
        } zipWith {
            validated3
        } zipWith {
            validated4
        }
}

interface Valid<T, E> : Validated<T, E> {

    val value: T
}

interface Invalid<T, E> : Validated<T, E> {

    val error: E
}

interface OrInvalidate<T, E> {

    infix fun orInvalidate(invalidator: (T) -> E): Validated<T, E>
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
