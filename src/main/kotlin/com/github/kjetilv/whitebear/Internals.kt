@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.github.kjetilv.whitebear

internal data class FailureList<E>(val str: (E) -> String = { "$it" }) : ErrorModel<E, List<E>> {

    override val empty =
        emptyList<E>()

    override fun isEmpty(aggregator: List<E>) =
        aggregator.isEmpty()

    override fun add(aggregator: List<E>, error: E): List<E> =
        aggregator + error

    override fun combine(aggregator1: List<E>, aggregator2: List<E>) =
        aggregator1 + aggregator2

    override fun str(aggregator: List<E>): String =
        aggregator.map { str }.joinToString { ", " }
}

@Suppress("UNCHECKED_CAST")
internal class ErrorModelValidationContext<E, A>(private val errorModel: ErrorModel<E, A>) : ValidatorContext<E, A> {

    internal fun flattenValidated(vals: List<Validated<*, A>>) =
        vals.map { it.error }
            .reduce { aggregator1: A, aggregator2: A ->
                errorModel.combine(aggregator1, aggregator2)
            }.takeUnless {
                errorModel isEmpty it
            }

    override fun <T> valid(value: T): Validated<T, A> = Valid(value)

    override fun <T> invalid(vararg failures: E): Validated<T, A> =
        Invalid(failures.toList()
            .foldRight(errorModel.empty) { error, aggregator ->
                errorModel.add(aggregator, error)
            })

    override fun <T> validateThat(value: T, test: (T) -> Boolean?): OrInvalidate<T, E, A> =
        Valid(value) validateThat test

    override fun <T> Validated<T, A>.validateThat(test: (T) -> Boolean?): OrInvalidate<T, E, A> =
        this.internals validateThat test

    override fun collect(vararg validated: Validated<*, A>): Validated<Any, A> =
        collectToT(validated.toList())

    override fun <T> Validated<T, A>.annotateInvalid(errorProvider: () -> E): Validated<T, A> =
        if (valid) this else Invalid(errorModel.add(error, errorProvider()))

    internal val <T> Validated<T, A>.internals
        get() = this as AbstractValidated<T>

    internal fun <T> collectToT(validated: List<Validated<*, A>>): Validated<T, A> =
        flattenValidated(validated)
            ?.let { Invalid(it) }
            ?: JustValid()

    @Suppress("UNCHECKED_CAST")
    internal abstract inner class AbstractValidated<T> : Validated<T, A> {

        override fun collect(vararg validated: Validated<*, A>): Validated<Any, A> =
            collectToT(listOf(this) + validated)

        internal abstract infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, A>

        internal fun sum(vararg validateds: Validated<*, A>): AbstractValidated<*> =
            flattenValidated(listOf(this) + validateds.toList())
                ?.let<A, Invalid<T>> { Invalid(it) }
                ?: this@AbstractValidated

        internal fun <V> retyped(): Invalid<V> =
            if (this.invalid) Invalid(error) else throw IllegalStateException("$this is valid")
    }

    internal inner class JustValid<T> : AbstractValidated<T>() {

        override val value: T get() = throw IllegalStateException("$this")

        override val error = errorModel.empty

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, A> =
            throw IllegalStateException("$this")

        override fun <R> map(mapping: (T) -> R): Validated<R, A> =
            throw IllegalStateException("$this")

        override fun <R> flatMap(mapping: (T) -> Validated<R, A>): Validated<R, A> =
            throw IllegalStateException("$this")

        override fun validValueOr(errorConsumer: (A) -> Nothing) =
            throw IllegalStateException("$this")

        override fun <R> zipWith(validator: () -> Validated<R, A>): Zipper1<T, R, A> =
            throw IllegalStateException("$this")

        override fun <R> ifValid(validator: () -> Validated<R, A>): Validated<R, A> = validator()

        override val valid = true

        override val invalid = false
    }

    internal inner class Valid<T>(private val item: T) : AbstractValidated<T>() {

        override val valid: Boolean = true

        override val invalid: Boolean = false

        override val value get() = item

        override val error = errorModel.empty

        override fun <R> map(mapping: (T) -> R): Validated<R, A> = Valid(mapping(item))

        override fun <R> flatMap(mapping: (T) -> Validated<R, A>): Validated<R, A> = mapping(item)

        override fun validValueOr(errorConsumer: (A) -> Nothing): T = item

        override fun <R> ifValid(validator: () -> Validated<R, A>): Validated<R, A> = validator()

        override infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, A> =
            object : OrInvalidate<T, E, A> {

                override fun elseInvalid(toError: (T) -> E): Validated<T, A> =
                    if (isValid(item) == true) this@Valid else Invalid(errorModel.add(error, toError(item)))
            }

        override fun <R> zipWith(validator0: () -> Validated<R, A>) =
            object : Zipper1<T, R, A> {

                override fun <V> map(combiner: (T, R) -> V): Validated<V, A> =
                    validator0().map { r -> combiner(item, r) }

                override fun <V> flatMap(combiner: (T, R) -> Validated<V, A>): Validated<V, A> =
                    validator0().flatMap { r -> combiner(item, r) }

                override fun <RR> zipWith(validator1: () -> Validated<RR, A>): Zipper2<T, R, RR, A> =
                    object : Zipper2<T, R, RR, A> {

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, A> =
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

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, A>): Validated<V, A> =
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

                        override fun <RRR> zipWith(validator2: () -> Validated<RRR, A>): Zipper3<T, R, RR, RRR, A> =
                            object : Zipper3<T, R, RR, RRR, A> {

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, A> =
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

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, A>): Validated<V, A> =
                                    validator0().let { validated0: Validated<R, A> ->
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

                                override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, A>): Zipper4<T, R, RR, RRR, RRRR, A> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, A> {

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, A> =
                                            validator0().let { validated0: Validated<R, A> ->
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

                                        override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, A>): Validated<V, A> =
                                            validator0().let { validated0: Validated<R, A> ->
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

                                        override val sum: Validated<*, A> get() = sum(validator0(), validator1(), validator2(), validator3())

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override val sum: Validated<*, A> get() = sum(validator0(), validator1(), validator2())

                                override fun toString(): String = javaClass.simpleName
                            }

                        override val sum: Validated<*, A> get() = sum(validator0(), validator1())

                        override fun toString(): String = javaClass.simpleName
                    }

                override val sum: Validated<*, A> get() = sum(validator0())
            }

        override fun toString() = "${javaClass.simpleName}[$item]"
    }

    internal inner class Invalid<T>(internal val validationError: A) : AbstractValidated<T>() {

        override val valid: Boolean = false

        override val invalid: Boolean = true

        override val value: T get() = throw IllegalStateException("$this invalid")

        override val error: A get() = validationError

        override fun <R> map(mapping: (T) -> R): Validated<R, A> =
            Invalid(validationError)

        override fun <R> flatMap(mapping: (T) -> Validated<R, A>): Invalid<R> =
            Invalid(validationError)

        override fun validValueOr(errorConsumer: (A) -> Nothing): Nothing =
            errorConsumer.invoke(validationError)

        override fun <R> zipWith(validator0: () -> Validated<R, A>): Zipper1<T, R, A> =
            object : Zipper1<T, R, A> {

                override fun <V> map(combiner: (T, R) -> V): Validated<V, A> =
                    Invalid(errorModel.combine(validationError, validator0().error))

                override fun <V> flatMap(combiner: (T, R) -> Validated<V, A>): Validated<V, A> =
                    Invalid(errorModel.combine(validationError, validator0().error))

                override fun <RR> zipWith(validator1: () -> Validated<RR, A>): Zipper2<T, R, RR, A> =

                    object : Zipper2<T, R, RR, A> {

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, A> =
                            sum(validator0(), validator1()).retyped()

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, A>): Validated<V, A> =
                            sum(validator0(), validator1()).retyped()

                        override fun <RRR> zipWith(validator2: () -> Validated<RRR, A>): Zipper3<T, R, RR, RRR, A> =
                            object : Zipper3<T, R, RR, RRR, A> {

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, A> =
                                    sum(validator0(), validator1(), validator2()).retyped()

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, A>): Validated<V, A> =
                                    sum(validator0(), validator1(), validator2()).retyped()

                                override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, A>): Zipper4<T, R, RR, RRR, RRRR, A> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, A> {

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, A> =
                                            sum(validator0(), validator1(), validator2(), validator3()).retyped()

                                        override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, A>): Validated<V, A> =
                                            sum(validator0(), validator1(), validator2(), validator3()).retyped()

                                        override val sum: Validated<*, A> get() = sum(validator0(), validator1(), validator2(), validator3())

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override val sum: Validated<*, A> get() = sum(validator0(), validator1(), validator2())

                                override fun toString(): String = javaClass.simpleName
                            }

                        override val sum: Validated<*, A> get() = sum(validator0(), validator1())

                        override fun toString(): String = javaClass.simpleName
                    }

                override val sum: Validated<*, A> get() = sum(validator0())

                override fun toString(): String = javaClass.simpleName
            }

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E, A> =
            object : OrInvalidate<T, E, A> {

                override fun elseInvalid(toErrors: (T) -> E): Validated<T, A> = this@Invalid
            }

        override fun toString() =
            "${javaClass.simpleName}[${
                errorModel str error
            }]"

        override fun <R> ifValid(validator: () -> Validated<R, A>): Validated<R, A> = this.retyped()
    }

    override fun toString() = "${javaClass.simpleName}[$errorModel]"
}
