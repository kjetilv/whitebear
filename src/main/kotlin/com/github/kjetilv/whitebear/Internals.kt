@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.github.kjetilv.whitebear

internal sealed class AbstractValidated<T, E> : Validated<T, E> {

    internal fun sum(vararg validateds: Validated<*, E>): AbstractValidated<*, E> =
        validateds.flatMap { it.errors }
            .takeIf { it.isNotEmpty() }
            ?.let { Invalid<T, E>(it) }
            ?: this

    internal fun <V> invalidSum(vararg validateds: Validated<*, E>): Invalid<V, E> =
        Invalid<T, E>((listOf(this) + validateds).flatMap { it.errors }).retyped()

    internal fun <V> retyped(): Invalid<V, E> =
        if (this.invalid) Invalid(errors) else throw IllegalStateException("$this is valid")

    internal val <T, E> Validated<T, E>.internals
        get() =
            this as AbstractValidated<T, E>
}

internal class JustValid<T, E> : AbstractValidated<T, E>() {

    override val value: T get() = throw IllegalStateException("$this")

    override val errors: Collection<E> = emptySet()

    override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
        throw IllegalStateException("$this")

    override fun <R> map(mapping: (T) -> R): Validated<R, E> =
        throw IllegalStateException("$this")

    override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> =
        throw IllegalStateException("$this")

    override fun validValueOr(errorsConsumer: (Collection<E>) -> Nothing) =
        throw IllegalStateException("$this")

    override fun <R> zipWith(validator: () -> Validated<R, E>): Zipper1<T, R, E> =
        throw IllegalStateException("$this")

    override fun annotateInvalidMulti(errorProvider: () -> Collection<E>): Validated<T, E> = this

    override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = validator()

    override val valid = true

    override val invalid = false
}

internal class Valid<T, E>(private val item: T) : AbstractValidated<T, E>() {

    override val valid: Boolean = true

    override val invalid: Boolean = false

    override val value get() = item

    override val errors: Collection<E> = emptySet()

    override fun <R> map(mapping: (T) -> R): Valid<R, E> = Valid(mapping(item))

    override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> = mapping(item)

    override fun annotateInvalidMulti(errorProvider: () -> Collection<E>): Validated<T, E> = this

    override fun validValueOr(errorsConsumer: (Collection<E>) -> Nothing): T = item

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

    override infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
        object : OrInvalidate<T, E> {

            override fun elseInvalid(toErrors: (T) -> E): Validated<T, E> =
                elseInvalids { listOf(toErrors(item)) }

            override fun elseInvalids(toErrors: (T) -> Collection<E>): Validated<T, E> =
                if (isValid(item) == true) this@Valid else Invalid(toErrors(item))
        }

    override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = validator()

    override fun toString() = "${javaClass.simpleName}[$item]"
}

internal class Invalid<T, E>(internal val errorCollector: Collection<E>) : AbstractValidated<T, E>() {

    internal constructor(error: E) : this(listOf(error))

    override val valid: Boolean = false

    override val invalid: Boolean = true

    override val value: T
        get() =
            throw IllegalStateException("$this invalid")

    override val errors: Collection<E>
        get() =
            errorCollector

    override fun <R> map(mapping: (T) -> R): Validated<R, E> =
        Invalid(errorCollector)

    override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Invalid<R, E> =
        Invalid(errorCollector)

    override fun annotateInvalidMulti(errorProvider: () -> Collection<E>): Invalid<T, E> =
        Invalid(errorCollector + errorProvider())

    override fun validValueOr(errorsConsumer: (Collection<E>) -> Nothing): Nothing =
        errorsConsumer.invoke(errorCollector)

    override fun <R> zipWith(validator0: () -> Validated<R, E>): Zipper1<T, R, E> =
        object : Zipper1<T, R, E> {

            override fun <V> map(combiner: (T, R) -> V): Invalid<V, E> =
                Invalid(errorCollector = errorCollector + validator0().errors)

            override fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E> =
                Invalid(errorCollector = errorCollector + validator0().errors)

            override fun <RR> zipWith(validator1: () -> Validated<RR, E>): Zipper2<T, R, RR, E> =

                object : Zipper2<T, R, RR, E> {

                    override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E> =
                        invalidSum(validator0(), validator1())

                    override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E> =
                        invalidSum(validator0(), validator1())

                    override fun <RRR> zipWith(validator2: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E> =
                        object : Zipper3<T, R, RR, RRR, E> {

                            override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E> =
                                invalidSum(validator0(), validator1(), validator2())

                            override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E> =
                                invalidSum(validator0(), validator1(), validator2())

                            override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E> =
                                object : Zipper4<T, R, RR, RRR, RRRR, E> {

                                    override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E> =
                                        invalidSum(validator0(), validator1(), validator2(), validator3())

                                    override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, E>): Validated<V, E> =
                                        invalidSum(validator0(), validator1(), validator2(), validator3())

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

    override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
        object : OrInvalidate<T, E> {

            override fun elseInvalid(toErrors: (T) -> E): Validated<T, E> = this@Invalid

            override fun elseInvalids(toErrors: (T) -> Collection<E>) = this@Invalid
        }

    override fun toString() = "${javaClass.simpleName}[${errorCollector.joinToString(" -> ")}}]"

    override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = this.retyped()
}

