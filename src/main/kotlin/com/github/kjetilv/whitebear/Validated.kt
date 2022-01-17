package com.github.kjetilv.whitebear

interface ValidationContext<E, A> {

    fun <T> valid(value: T): Validated<T, A>

    fun <T> invalid(vararg failures: E): Validated<T, A>

    fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E, A>

    infix fun <T> Validated<T, A>.validateThat(test: (T) -> Boolean?): OrInvalidate<T, E, A>

    infix fun <T> Validated<T, A>.annotateInvalidated(errorProvider: () -> E): Validated<T, A>

    fun collect(vararg validated: Validated<*, A>): Validated<Any, A>
}

interface Validated<T, A> {

    val value: T

    val error: A

    infix fun <R> map(mapping: (T) -> R): Validated<R, A>

    infix fun <R> flatMap(mapping: (T) -> Validated<R, A>): Validated<R, A>

    infix fun <R> zipWith(
        validated: Validated<R, A>,
    ): Zipper1<T, R, A> =
        this zipWith { validated }

    infix fun <R> zipWith(
        validator: () -> Validated<R, A>,
    ): Zipper1<T, R, A>

    fun <R, RR> zipWith(
        validated1: Validated<R, A>,
        validated2: Validated<RR, A>,
    ) =
        this zipWith {
            validated1
        } zipWith {
            validated2
        }

    fun <R, RR, RRR> zipWith(
        validated1: Validated<R, A>,
        validated2: Validated<RR, A>,
        validated3: Validated<RRR, A>,
    ) =
        this zipWith {
            validated1
        } zipWith {
            validated2
        } zipWith {
            validated3
        }

    fun <R, RR, RRR, RRRR> zipWith(
        validated1: Validated<R, A>,
        validated2: Validated<RR, A>,
        validated3: Validated<RRR, A>,
        validated4: Validated<RRRR, A>,
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

    infix fun valueOr(errorConsumer: (A) -> Nothing): T

    fun valueOrNull(): T?

    val valid: Boolean

    val invalid: Boolean
}

interface OrInvalidate<T, E, A> {

    infix fun orInvalidate(invalidator: (T) -> E): Validated<T, A>
}

interface Zipper1<T, R, A> {

    infix fun <RR> zipWith(validated: Validated<RR, A>) = zipWith { validated }

    infix fun <RR> zipWith(validator: () -> Validated<RR, A>): Zipper2<T, R, RR, A>

    infix fun <O> map(combiner: (T, R) -> O): Validated<O, A>

    infix fun <O> flatMap(combiner: (T, R) -> Validated<O, A>): Validated<O, A>

    val sum: Validated<*, A>
}

interface Zipper2<T, R, RR, A> {

    infix fun <RRR> zipWith(validated: Validated<RRR, A>) = zipWith { validated }

    infix fun <RRR> zipWith(validator: () -> Validated<RRR, A>): Zipper3<T, R, RR, RRR, A>

    infix fun <O> map(combiner: (T, R, RR) -> O): Validated<O, A>

    infix fun <O> flatMap(combiner: (T, R, RR) -> Validated<O, A>): Validated<O, A>

    val sum: Validated<*, A>
}

interface Zipper3<T, R, RR, RRR, A> {

    infix fun <RRRR> zipWith(validated: Validated<RRRR, A>) = zipWith { validated }

    infix fun <RRRR> zipWith(validator: () -> Validated<RRRR, A>): Zipper4<T, R, RR, RRR, RRRR, A>

    infix fun <O> map(combiner: (T, R, RR, RRR) -> O): Validated<O, A>

    infix fun <O> flatMap(combiner: (T, R, RR, RRR) -> Validated<O, A>): Validated<O, A>

    val sum: Validated<*, A>
}

interface Zipper4<T, R, RR, RRR, RRRR, A> {

    infix fun <O> map(combiner: (T, R, RR, RRR, RRRR) -> O): Validated<O, A>

    infix fun <O> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<O, A>): Validated<O, A>

    val sum: Validated<*, A>
}
