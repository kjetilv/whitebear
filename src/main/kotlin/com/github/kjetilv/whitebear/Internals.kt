@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.github.kjetilv.whitebear

internal data class FailureList<F>(val str: (F) -> String = { "$it" }) : ErrorModel<F, List<F>> {

    override val empty =
        emptyList<F>()

    override fun isEmpty(e: List<F>) =
        e.isEmpty()

    override fun add(e: List<F>, c: F): List<F> =
        e + c

    override fun combine(e1: List<F>, e2: List<F>) =
        e1 + e2

    override fun str(es: List<F>): String =
        es.map { str }.joinToString { ", " }
}

@Suppress("UNCHECKED_CAST")
internal class ErrorModelValidationContext<F, E>(private val errorModel: ErrorModel<F, E>) : ValidatorContext<E, F> {

    internal fun flattenValidated(vals: List<Validated<*, E>>) =
        vals.map { it.error ?: errorModel.empty }
            .reduce { e1, e2 ->
                errorModel.combine(e1, e2)
            }.takeUnless {
                errorModel isEmpty it
            }

    override fun <T> valid(value: T): Validated<T, E> = Valid(value)

    override fun <T> invalid(vararg failures: F): Validated<T, E> =
        Invalid(failures.toList()
            .foldRight(errorModel.empty) { f, e ->
                errorModel.add(e, f)
            })

    override fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E, F> =
        Valid(value) validateThat test

    override fun <T> Validated<T, E>.validateThat(test: (T) -> Boolean?): OrInvalidate<T, E, F> =
        this.internals validateThat test

    override fun collect(vararg validated: Validated<*, E>): Validated<Any, E> =
        collectToT(validated.toList())

    override fun <T> Validated<T, E>.annotateInvalid(errorProvider: () -> F): Validated<T, E> =
        if (valid) this else Invalid(errorModel.add(error, errorProvider()))

    internal val <T> Validated<T, E>.internals
        get() = this as AbstractValidated<T>

    internal fun <T> collectToT(validated: List<Validated<*, E>>): Validated<T, E> =
        flattenValidated(validated)
            ?.let { Invalid(it) }
            ?: JustValid()

    @Suppress("UNCHECKED_CAST")
    internal abstract inner class AbstractValidated<T> : Validated<T, E> {

        override fun collect(vararg validated: Validated<*, E>): Validated<Any, E> =
            collectToT(listOf(this) + validated)

        internal abstract infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, F>

        internal fun sum(vararg validateds: Validated<*, E>): AbstractValidated<*> =
            flattenValidated(listOf(this) + validateds.toList())
                ?.let<E, Invalid<T>> { Invalid(it) }
                ?: this@AbstractValidated

        internal fun <V> retyped(): Invalid<V> =
            if (this.invalid) Invalid(error) else throw IllegalStateException("$this is valid")
    }

    internal inner class JustValid<T> : AbstractValidated<T>() {

        override val value: T get() = throw IllegalStateException("$this")

        override val error = errorModel.empty

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, F> =
            throw IllegalStateException("$this")

        override fun <R> map(mapping: (T) -> R): Validated<R, E> =
            throw IllegalStateException("$this")

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> =
            throw IllegalStateException("$this")

        override fun validValueOr(errorConsumer: (E) -> Nothing) =
            throw IllegalStateException("$this")

        override fun <R> zipWith(validator: () -> Validated<R, E>): Zipper1<T, R, E> =
            throw IllegalStateException("$this")

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = validator()

        override val valid = true

        override val invalid = false
    }

    internal inner class Valid<T>(private val item: T) : AbstractValidated<T>() {

        override val valid: Boolean = true

        override val invalid: Boolean = false

        override val value get() = item

        override val error = errorModel.empty

        override fun <R> map(mapping: (T) -> R): Validated<R, E> = Valid(mapping(item))

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> = mapping(item)

        override fun validValueOr(errorConsumer: (E) -> Nothing): T = item

        override fun <R> zipWith(validator0: () -> Validated<R, E>) =
            object : Zipper1<T, R, E> {

                override fun <V> map(combiner: (T, R) -> V): Validated<V, E> =
                    validator0().map { r -> combiner(item, r) }

                override fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E> =
                    validator0().flatMap { r -> combiner(item, r) }

                override fun <RR> zipWith(validator1: () -> Validated<RR, E>): Zipper2<T, R, RR, E> =
                    object : Zipper2<T, R, RR, E> {

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E> =
                            validator0().let { validated0 ->
                                validator1().let { validated1 ->
                                    sum(validated0, validated1).takeIf { it.invalid }
                                        ?.internals?.retyped()
                                        ?: validated0.flatMap { itemR ->
                                            validated1.map { itemRR ->
                                                combiner(item, itemR, itemRR)
                                            }
                                        }
                                }
                            }

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E> =
                            validator0().let { validated0 ->
                                validator1().let { validated1 ->
                                    sum(validated0, validated1)
                                        .takeIf { it.invalid }
                                        ?.internals?.retyped()
                                        ?: validated0.flatMap { itemR ->
                                            validated1.flatMap { itemRR ->
                                                combiner(item, itemR, itemRR)
                                            }
                                        }
                                }
                            }

                        override fun <RRR> zipWith(validator2: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E> =
                            object : Zipper3<T, R, RR, RRR, E> {

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E> =
                                    validator0().let { validated0 ->
                                        validator1().let { validated1 ->
                                            validator2().let { validated2 ->
                                                sum(validated0, validated1, validated2)
                                                    .takeIf { it.invalid }
                                                    ?.internals?.retyped()
                                                    ?: validated0.flatMap { itemR ->
                                                        validated1.flatMap { itemRR ->
                                                            validated2.map { itemRRR ->
                                                                combiner(item, itemR, itemRR, itemRRR)
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E> =
                                    validator0().let { validated0: Validated<R, E> ->
                                        validator1().let { validated1 ->
                                            validator2().let { validated2 ->
                                                sum(validated0, validated1, validated2)
                                                    .takeIf { it.invalid }
                                                    ?.internals?.retyped()
                                                    ?: validated0.flatMap { itemR ->
                                                        validated1.flatMap { itemRR ->
                                                            validated2.flatMap { itemRRR ->
                                                                combiner(item, itemR, itemRR, itemRRR)
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }

                                override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, E> {

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E> =
                                            validator0().let { validated0: Validated<R, E> ->
                                                validator1().let { validated1 ->
                                                    validator2().let { validated2 ->
                                                        validator3().let { validated3 ->
                                                            sum(validated0, validated1, validated2, validated3)
                                                                .takeIf { it.invalid }
                                                                ?.internals?.retyped()
                                                                ?: validated0.flatMap { itemR ->
                                                                    validated1.flatMap { itemRR ->
                                                                        validated2.flatMap { itemRRR ->
                                                                            validated3.map { itemRRRR ->
                                                                                combiner(item, itemR, itemRR, itemRRR, itemRRRR)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }
                                            }

                                        override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, E>): Validated<V, E> =
                                            validator0().let { validated0: Validated<R, E> ->
                                                validator1().let { validated1 ->
                                                    validator2().let { validated2 ->
                                                        validator3().let { validated3 ->
                                                            sum(validated0, validated1, validated2, validated3)
                                                                .takeIf { it.invalid }
                                                                ?.internals?.retyped()
                                                                ?: validated0.flatMap { itemR ->
                                                                    validated1.flatMap { itemRR ->
                                                                        validated2.flatMap { itemRRR ->
                                                                            validated3.flatMap { itemRRRR ->
                                                                                combiner(item, itemR, itemRR, itemRRR, itemRRRR)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }
                                            }

                                        override val sum: Validated<*, E> get() = sum(validator0(), validator1(), validator2(), validator3())

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override val sum: Validated<*, E> get() = sum(validator0(), validator1(), validator2())

                                override fun toString(): String = javaClass.simpleName
                            }

                        override val sum: Validated<*, E> get() = sum(validator0(), validator1())

                        override fun toString(): String = javaClass.simpleName
                    }

                override val sum: Validated<*, E> get() = sum(validator0())
            }

        override infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, F> =
            object : OrInvalidate<T, E, F> {

                override fun elseInvalid(toError: (T) -> F): Validated<T, E> =
                    if (isValid(item) == true) this@Valid else Invalid(errorModel.add(error, toError(item)))
            }

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = validator()

        override fun toString() = "${javaClass.simpleName}[$item]"
    }

    internal inner class Invalid<T>(internal val validationError: E) : AbstractValidated<T>() {

        override val valid: Boolean = false

        override val invalid: Boolean = true

        override val value: T get() = throw IllegalStateException("$this invalid")

        override val error: E get() = validationError

        override fun <R> map(mapping: (T) -> R): Validated<R, E> =
            Invalid(validationError)

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Invalid<R> =
            Invalid(validationError)

        override fun validValueOr(errorConsumer: (E) -> Nothing): Nothing =
            errorConsumer.invoke(validationError)

        override fun <R> zipWith(validator0: () -> Validated<R, E>): Zipper1<T, R, E> =
            object : Zipper1<T, R, E> {

                override fun <V> map(combiner: (T, R) -> V): Validated<V, E> =
                    Invalid(errorModel.combine(validationError, validator0().error))

                override fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E> =
                    Invalid(errorModel.combine(validationError, validator0().error))

                override fun <RR> zipWith(validator1: () -> Validated<RR, E>): Zipper2<T, R, RR, E> =

                    object : Zipper2<T, R, RR, E> {

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E> =
                            sum(validator0(), validator1()).retyped()

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E> =
                            sum(validator0(), validator1()).retyped()

                        override fun <RRR> zipWith(validator2: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E> =
                            object : Zipper3<T, R, RR, RRR, E> {

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E> =
                                    sum(validator0(), validator1(), validator2()).retyped()

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E> =
                                    sum(validator0(), validator1(), validator2()).retyped()

                                override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, E> {

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E> =
                                            sum(validator0(), validator1(), validator2(), validator3()).retyped()

                                        override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, E>): Validated<V, E> =
                                            sum(validator0(), validator1(), validator2(), validator3()).retyped()

                                        override val sum: Validated<*, E> get() = sum(validator0(), validator1(), validator2(), validator3())

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override val sum: Validated<*, E> get() = sum(validator0(), validator1(), validator2())

                                override fun toString(): String = javaClass.simpleName
                            }

                        override val sum: Validated<*, E> get() = sum(validator0(), validator1())

                        override fun toString(): String = javaClass.simpleName
                    }

                override val sum: Validated<*, E> get() = sum(validator0())

                override fun toString(): String = javaClass.simpleName
            }

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, F> =
            object : OrInvalidate<T, E, F> {

                override fun elseInvalid(toErrors: (T) -> F): Validated<T, E> = this@Invalid
            }

        override fun toString() =
            "${javaClass.simpleName}[${
                errorModel str error
            }]"

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = this.retyped()
    }

    override fun toString() = "${javaClass.simpleName}[$errorModel]"
}
