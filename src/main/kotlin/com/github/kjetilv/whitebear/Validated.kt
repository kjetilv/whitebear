@file:Suppress("unused")

package com.github.kjetilv.whitebear

fun <T, E> validated(action: ValidatorContext<E>.() -> Validated<T, E>): Validated<T, E> =
    action(object : ValidatorContext<E> {})

fun <T, E> valid(value: T): Validated<T, E> = Valid(value)

fun <T, E> invalid(vararg error: E): Validated<T, E> = Invalid(error.toList())

fun <T, E> validateThat(value: T, test: (T) -> Boolean?) =
    Valid<T, E>(value) validateThat test

fun <T, E> collectToT(
    vararg validated: Validated<*, E>
): Validated<T, E> =
    validated.toList()
        .flatMap { it.errors }
        .takeIf { it.isNotEmpty() }
        ?.let { Invalid(it) }
        ?: JustValid()

fun <T, R, E, V> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
    combiner: (T, R) -> V,
): Validated<V, E> =
    zip(v, v1) map combiner

fun <T, R, RR, E, V> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
    v2: Validated<RR, E>,
    combiner: (T, R, RR) -> V,
): Validated<V, E> =
    zip(v, v1, v2) map combiner

fun <T, R, RR, RRR, E, V> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
    v2: Validated<RR, E>,
    v3: Validated<RRR, E>,
    combiner: (T, R, RR, RRR) -> V,
): Validated<V, E> =
    zip(v, v1, v2, v3) map combiner

fun <T, R, E> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
): Zipper1<T, R, E> =
    v zipWith v1

fun <T, R, RR, E> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
    v2: Validated<RR, E>,
): Zipper2<T, R, RR, E> =
    v zipWith v1 zipWith v2

fun <T, R, RR, RRR, E> zip(
    v: Validated<T, E>,
    v1: Validated<R, E>,
    v2: Validated<RR, E>,
    v3: Validated<RRR, E>,
): Zipper3<T, R, RR, RRR, E> =
    v zipWith v1 zipWith v2 zipWith v3

interface ValidatorContext<E> {

    fun <T> valid(value: T): Validated<T, E> =
        valid<T, E>(value)

    fun <T> invalid(vararg error: E): Validated<T, E> =
        invalid<T, E>(*error)

    fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E> =
        validateThat<T, E>(value, test)

    fun collect(vararg validated: Validated<*, E>): Validated<Any, E> =
        collectToT(*validated)

    fun <T> collectToT(vararg validated: Validated<*, E>): Validated<T, E> =
        collectToT<T, E>(*validated)

    fun <T, R, RR, V> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        combiner: (T, R, RR) -> V,
    ): Validated<V, E> =
        zip<T, R, RR, E, V>(v, v1, v2, combiner)

    fun <T, R> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
    ): Zipper1<T, R, E> =
        zip<T, R, E>(v, v1)

    fun <T, R, RR> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
    ): Zipper2<T, R, RR, E> =
        zip<T, R, RR, E>(v, v1, v2)

    fun <T, R, RR, RRR> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        v3: Validated<RRR, E>,
    ): Zipper3<T, R, RR, RRR, E> =
        zip<T, R, RR, RRR, E>(v, v1, v2, v3)

    fun <T, R, RR, RRR, V> zip(
        v: Validated<T, E>,
        v1: Validated<R, E>,
        v2: Validated<RR, E>,
        v3: Validated<RRR, E>,
        combiner: (T, R, RR, RRR) -> V,
    ): Validated<V, E> =
        zip<T, R, RR, RRR, E, V>(v, v1, v2, v3, combiner)
}

sealed interface Validated<T, E> {

    val value: T

    val errors: Collection<E>

    infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E>

    infix fun <R> map(mapping: (T) -> R): Validated<R, E>

    infix fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E>

    infix fun <R> zipWith(
        validated: Validated<R, E>,
    ): Zipper1<T, R, E> =
        zipWith { validated }

    fun <R, RR> zipWith(
        validated1: Validated<R, E>,
        validated2: Validated<RR, E>
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

    infix fun annotateInvalid(errorProvider: () -> E) =
        annotateInvalidMulti { listOf(errorProvider()) }

    infix fun annotateInvalidMulti(errorProvider: () -> Collection<E>): Validated<T, E>

    infix fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E>

    infix fun validValueOr(errorsConsumer: (Collection<E>) -> Nothing): T

    val valid: Boolean

    val invalid: Boolean
}

interface OrInvalidate<T, E> {

    infix fun elseInvalid(toErrors: (T) -> E): Validated<T, E>

    infix fun elseInvalids(toErrors: (T) -> Collection<E>): Validated<T, E>
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
