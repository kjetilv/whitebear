@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "DuplicatedCode")

package com.github.kjetilv.whitebear

fun <E> errorProcessorValidationContext(errorProcessor: ErrorProcessor<E>): ValidationContext<E> =
    ErrorProcessorValidationContext(errorProcessor)

private data class ErrorProcessorValidationContext<E>(private val errorProcessor: ErrorProcessor<E>) :
    ValidationContext<E> {

    fun flattenValidated(vals: List<Validated<*, E>>) =
        vals.filterIsInstance<Invalid<*, E>>().map { it.error }
            .takeUnless { it.isEmpty() }
            ?.reduce { aggregator1: E, aggregator2: E ->
                errorProcessor.combine(aggregator1, aggregator2)
            }.takeUnless {
                it == errorProcessor.empty
            }
            ?: errorProcessor.empty

    override fun <T> valid(value: T): Validated<T, E> = ValidatedOK(value)

    override fun <T> valid(value: () -> T): Validated<T, E> = ValidatedOK(value())

    override fun <T> invalid(failure: E): Validated<T, E> = ValidatedFailed(failure)

    override fun <T> invalid(failure: () -> E): Validated<T, E> = ValidatedFailed(failure())

    override fun <T> validIf(value: T, test: (T) -> Boolean?): OrInvalidate<T, E> =
        ValidatedOK(value) validateThat test

    override fun <T> Validated<T, E>.validIf(test: (T) -> Boolean?): OrInvalidate<T, E> = this validateThat test

    override fun <T> Validated<T, E>.withViolations(violations: (T) -> E?): Validated<T, E> =
        this applyViolations violations

    override fun collect(vararg validated: Validated<*, E>): Validated<Any, E> =
        flattenValidated(validated.toList())
            ?.takeUnless { it == errorProcessor.empty }
            ?.let<E, ValidatedFailed<Any>> { ValidatedFailed(it) }
            ?: JustValid()

    override fun <T> Validated<T, E>.annotateInvalidated(errorProvider: () -> E): Validated<T, E> =
        when (this) {
            is Valid<T, E> ->
                this
            is Invalid<T, E> ->
                ValidatedFailed(errorProcessor.combine(this.error, errorProvider()))
            else ->
                throw IllegalStateException("$this not supported")
        }

    private abstract inner class AbstractValidated<T> : Validated<T, E> {

        abstract fun <R> retyped(): AbstractValidated<R>

        fun sum(vararg validateds: Validated<*, E>): AbstractValidated<*> =
            flattenValidated(listOf(this) + validateds.toList())
                ?.takeUnless { it == errorProcessor.empty }
                ?.let<E, ValidatedFailed<T>> { ValidatedFailed(it) }
                ?: this@AbstractValidated
    }

    private inner class JustValid<T> : AbstractValidated<T>(), Valid<T, E> {

        override val value: T get() = throw IllegalStateException("$this")

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
            throw IllegalStateException("$this")

        override fun <R> map(mapping: (T) -> R): Validated<R, E> = throw IllegalStateException("$this")

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> =
            throw IllegalStateException("$this")

        override fun applyViolation(violation: (T) -> E?): ValidatedFailed<T> =
            throw IllegalStateException("$this")

        override fun applyViolations(violations: (T) -> E?): ValidatedFailed<T> =
            throw IllegalStateException("$this")

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> =
            validator()

        override fun valueOr(errorConsumer: (E) -> Nothing) =
            throw IllegalStateException("$this")

        override val valueOrNull: Nothing
            get() =
                throw IllegalStateException("$this")

        override fun <R> zipWith(validator: () -> Validated<R, E>): Zipper1<T, R, E> =
            throw IllegalStateException("$this")

        override fun <R> retyped(): AbstractValidated<R> = JustValid()

        override val valid = true

        override val invalid = false
    }

    private inner class ValidatedOK<T>(private val item: T) : AbstractValidated<T>(), Valid<T, E> {
        override val valid: Boolean = true

        override val invalid: Boolean = false

        override val value get() = item

        override fun <R> map(mapping: (T) -> R): Validated<R, E> = ValidatedOK(mapping(item))

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): Validated<R, E> = mapping(item)

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> = validator()

        override fun applyViolations(violations: (T) -> E?): Validated<T, E> =
            violations(item)
                ?.takeUnless { it == errorProcessor.empty }
                ?.let { ValidatedFailed(it) }
                ?: this

        override fun applyViolation(violation: (T) -> E?): Validated<T, E> =
            violation(item)?.let { ValidatedFailed(it) } ?: this

        override fun valueOr(errorConsumer: (E) -> Nothing): T = item

        override val valueOrNull: T = item

        override infix fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
            object : OrInvalidate<T, E> {

                override fun orInvalidate(invalidator: (T) -> E): Validated<T, E> =
                    (if (isValid(item) == true)
                        this@ValidatedOK
                    else
                        ValidatedFailed(invalidator(item)))
            }

        override fun <R> retyped(): AbstractValidated<R> =
            throw IllegalStateException("$this")

        override fun <R> zipWith(validator0: () -> Validated<R, E>) =
            object : Zipper1<T, R, E> {

                override val sum: Validated<*, E> get() = sum(validator0())

                override fun <V> map(combiner: (T, R) -> V): Validated<V, E> =
                    validator0().map { r -> combiner(item, r) }

                override fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E> =
                    validator0().flatMap { r -> combiner(item, r) }

                override fun <RR> zipWith(validator1: () -> Validated<RR, E>): Zipper2<T, R, RR, E> =
                    object : Zipper2<T, R, RR, E> {

                        override val sum: Validated<*, E> get() = sum(validator0(), validator1())

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E> =
                            validator0().let { validated0 ->
                                validator1().let { validated1 ->
                                    sum(validated0, validated1).takeIf { it.invalid }
                                        ?.retyped()
                                        ?: (validated0 flatMap { itemR ->
                                            validated1 map { itemRR ->
                                                combiner(item, itemR, itemRR)
                                            }
                                        })
                                }
                            }

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E> =
                            validator0().let { validated0 ->
                                validator1().let { validated1 ->
                                    sum(validated0, validated1)
                                        .takeIf { it.invalid }
                                        ?.retyped()
                                        ?: (validated0 flatMap { itemR ->
                                            validated1 flatMap { itemRR ->
                                                combiner(item, itemR, itemRR)
                                            }
                                        })
                                }
                            }

                        override fun <RRR> zipWith(validator2: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E> =
                            object : Zipper3<T, R, RR, RRR, E> {

                                override val sum: Validated<*, E> get() = sum(validator0(), validator1(), validator2())

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E> =
                                    validator0().let { validated0 ->
                                        validator1().let { validated1 ->
                                            validator2().let { validated2 ->
                                                sum(validated0, validated1, validated2)
                                                    .takeIf { it.invalid }
                                                    ?.retyped()
                                                    ?: (validated0 flatMap { itemR ->
                                                        validated1 flatMap { itemRR ->
                                                            validated2 map { itemRRR ->
                                                                combiner(item, itemR, itemRR, itemRRR)
                                                            }
                                                        }
                                                    })
                                            }
                                        }
                                    }

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E> =
                                    validator0().let { validated0: Validated<R, E> ->
                                        validator1().let { validated1 ->
                                            validator2().let { validated2 ->
                                                sum(validated0, validated1, validated2)
                                                    .takeIf { it.invalid }
                                                    ?.retyped()
                                                    ?: (validated0 flatMap { itemR ->
                                                        validated1 flatMap { itemRR ->
                                                            validated2 flatMap { itemRRR ->
                                                                combiner(item, itemR, itemRR, itemRRR)
                                                            }
                                                        }
                                                    })
                                            }
                                        }
                                    }

                                override fun <RRRR> zipWith(validator3: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, E> {

                                        override val sum: Validated<*, E>
                                            get() = sum(validator0(),
                                                validator1(),
                                                validator2(),
                                                validator3())

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E> =
                                            validator0().let { validated0: Validated<R, E> ->
                                                validator1().let { validated1 ->
                                                    validator2().let { validated2 ->
                                                        validator3().let { validated3 ->
                                                            sum(validated0, validated1, validated2, validated3)
                                                                .takeIf { it.invalid }
                                                                ?.retyped()
                                                                ?: (validated0 flatMap { itemR ->
                                                                    validated1 flatMap { itemRR ->
                                                                        validated2 flatMap { itemRRR ->
                                                                            validated3 map { itemRRRR ->
                                                                                combiner(item,
                                                                                    itemR,
                                                                                    itemRR,
                                                                                    itemRRR,
                                                                                    itemRRRR)
                                                                            }
                                                                        }
                                                                    }
                                                                })
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
                                                                ?.retyped()
                                                                ?: (validated0 flatMap { itemR ->
                                                                    validated1 flatMap { itemRR ->
                                                                        validated2 flatMap { itemRRR ->
                                                                            validated3 flatMap { itemRRRR ->
                                                                                combiner(item,
                                                                                    itemR,
                                                                                    itemRR,
                                                                                    itemRRR,
                                                                                    itemRRRR)
                                                                            }
                                                                        }
                                                                    }
                                                                })
                                                        }
                                                    }
                                                }
                                            }

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override fun toString(): String = javaClass.simpleName
                            }

                        override fun toString(): String = javaClass.simpleName
                    }

                override fun toString(): String = javaClass.simpleName
            }

        override fun toString() = "${javaClass.simpleName}[$item]"
    }

    private inner class ValidatedFailed<T>(private val validationError: E) : AbstractValidated<T>(), Invalid<T, E> {

        override val valid: Boolean = false

        override val invalid: Boolean = true

        override val error: E get() = validationError

        override fun <R> map(mapping: (T) -> R): Validated<R, E> =
            ValidatedFailed(validationError)

        override fun <R> flatMap(mapping: (T) -> Validated<R, E>): ValidatedFailed<R> =
            ValidatedFailed(validationError)

        override fun <R> retyped(): ValidatedFailed<R> = ValidatedFailed(validationError)

        override fun <R> ifValid(validator: () -> Validated<R, E>): Validated<R, E> =
            this.retyped()

        override fun applyViolations(violations: (T) -> E?): Validated<T, E> =
            this

        override fun applyViolation(violation: (T) -> E?): Validated<T, E> =
            this

        override fun valueOr(errorConsumer: (E) -> Nothing): Nothing =
            errorConsumer.invoke(validationError)

        override val valueOrNull: T? = null

        override fun <R> zipWith(validator1: () -> Validated<R, E>): Zipper1<T, R, E> =
            object : Zipper1<T, R, E> {

                override fun <V> map(combiner: (T, R) -> V): Validated<V, E> =
                    sum(validator1()).retyped()

                override
                fun <V> flatMap(combiner: (T, R) -> Validated<V, E>): Validated<V, E> =
                    sum(validator1()).retyped()

                override val sum: Validated<*, E> get() = sum(validator1())

                override fun <RR> zipWith(validator2: () -> Validated<RR, E>): Zipper2<T, R, RR, E> =

                    object : Zipper2<T, R, RR, E> {

                        override val sum: Validated<*, E> get() = sum(validator1(), validator2())

                        override fun <V> map(combiner: (T, R, RR) -> V): Validated<V, E> =
                            sum(validator1(), validator2()).retyped()

                        override fun <V> flatMap(combiner: (T, R, RR) -> Validated<V, E>): Validated<V, E> =
                            sum(validator1(), validator2()).retyped()

                        override fun <RRR> zipWith(validator3: () -> Validated<RRR, E>): Zipper3<T, R, RR, RRR, E> =
                            object : Zipper3<T, R, RR, RRR, E> {

                                override val sum: Validated<*, E> get() = sum(validator1(), validator2(), validator3())

                                override fun <V> map(combiner: (T, R, RR, RRR) -> V): Validated<V, E> =
                                    sum(validator1(), validator2(), validator3()).retyped()

                                override fun <V> flatMap(combiner: (T, R, RR, RRR) -> Validated<V, E>): Validated<V, E> =
                                    sum(validator1(), validator2(), validator3()).retyped()

                                override fun <RRRR> zipWith(validator4: () -> Validated<RRRR, E>): Zipper4<T, R, RR, RRR, RRRR, E> =
                                    object : Zipper4<T, R, RR, RRR, RRRR, E> {

                                        override val sum: Validated<*, E>
                                            get() = sum(validator1(),
                                                validator2(),
                                                validator3(),
                                                validator4())

                                        override fun <V> map(combiner: (T, R, RR, RRR, RRRR) -> V): Validated<V, E> =
                                            sum(validator1(), validator2(), validator3(), validator4()).retyped()

                                        override fun <V> flatMap(combiner: (T, R, RR, RRR, RRRR) -> Validated<V, E>): Validated<V, E> =
                                            sum(validator1(), validator2(), validator3(), validator4()).retyped()

                                        override fun toString(): String = javaClass.simpleName
                                    }

                                override fun toString(): String = javaClass.simpleName
                            }

                        override fun toString(): String = javaClass.simpleName
                    }

                override fun toString(): String = javaClass.simpleName
            }

        override fun validateThat(isValid: (T) -> Boolean?): OrInvalidate<T, E> =
            object : OrInvalidate<T, E> {

                override fun orInvalidate(invalidator: (T) -> E): Validated<T, E> = this@ValidatedFailed
            }

        override fun toString() = "${javaClass.simpleName}[${error}]"
    }

    override fun toString() = "${javaClass.simpleName}[$errorProcessor]"
}
