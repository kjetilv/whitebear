package im.tanin.valimon

fun <T, E> valid(value: T): Validated<T, E> = Valid(value)

fun <T, E> invalid(error: E): Validated<T, E> = Invalid(error)

interface Invalidator<T, E> {

    fun orFail(ef: (T) -> E): Validated<T, E>
}

sealed interface Validated<T, E> {

    val value: T?

    val errors: List<E>

    fun validateThat(predicate: (T) -> Boolean?): Invalidator<T, E>

    fun <R> map(f: (T) -> R): Validated<R, E>

    fun <R> flatMap(f: (T) -> Validated<R, E>): Validated<R, E>

    fun whenInvalid(e: () -> E): Validated<T, E>

    fun getOrElse(failure: (List<E>) -> Nothing): T

    fun <R> zipWith(validated: () -> Validated<R, E>): Combiner<T, R, E>

    val validated: Boolean
}

interface Combiner<T, R, E> {

    fun <V> combineTo(f: (T, R) -> V): Validated<V, E>

    fun <RR> zipWith(validated2: () -> Validated<RR, E>): Combiner2<T, R, RR, E>
}

interface Combiner2<T, R, RR, E> {

    fun <V> combineTo(f2: (T, R, RR) -> V): Validated<V, E>
}

internal class Valid<T, E>(private val t: T) : Validated<T, E> {

    override val validated: Boolean = true

    override val value get() = t

    override val errors = emptyList<E>()

    override fun <R> map(f: (T) -> R) = Valid<R, E>(f(t))

    override fun <R> flatMap(f: (T) -> Validated<R, E>): Validated<R, E> = f(t)

    override fun whenInvalid(e: () -> E): Validated<T, E> = this

    override fun getOrElse(failure: (List<E>) -> Nothing) = t

    override fun <R> zipWith(validated: () -> Validated<R, E>) =
        object : Combiner<T, R, E> {

            override fun <V> combineTo(f: (T, R) -> V) =
                validated().map { r -> f(t, r) }

            override fun <RR> zipWith(validated2: () -> Validated<RR, E>) =
                object : Combiner2<T, R, RR, E> {

                    override fun <V> combineTo(f2: (T, R, RR) -> V) =
                        validated().flatMap { r ->
                            validated2().map { r2 -> f2(t, r, r2) }
                        }
                }
        }

    override fun validateThat(predicate: (T) -> Boolean?) =
        object : Invalidator<T, E> {
        override fun orFail(ef: (T) -> E) =
            if (predicate(t) == true) Valid(t) else Invalid<T, E>(ef(t))
    }
    override fun toString() = "${javaClass.simpleName}[$t]"
}

internal class Invalid<T, E>(internal val es: List<E>) : Validated<T, E> {

    override val validated: Boolean = false

    internal constructor(e: E) : this(listOf(e))

    override val value: T? get() = null

    override val errors get() = es

    override fun <R> map(f: (T) -> R) = Invalid<R, E>(es)

    override fun <R> flatMap(f: (T) -> Validated<R, E>) = Invalid<R, E>(es)

    override fun whenInvalid(e: () -> E): Invalid<T, E> = Invalid(es + e())

    override fun getOrElse(failure: (List<E>) -> Nothing) = failure.invoke(es)

    override fun <R> zipWith(validated: () -> Validated<R, E>): Combiner<T, R, E> =
        object : Combiner<T, R, E> {

            override fun <V> combineTo(f: (T, R) -> V) =
                Invalid<V, E>(es = validated().errors)

            override fun <RR> zipWith(validated2: () -> Validated<RR, E>) =
                object : Combiner2<T, R, RR, E> {
                    override fun <V> combineTo(f2: (T, R, RR) -> V) =
                        Invalid<V, E>(es + validated().errors + validated2().errors)
                }
        }

    override fun validateThat(predicate: (T) -> Boolean?): Invalidator<T, E> =
        object : Invalidator<T, E> {
            override fun orFail(ef: (T) -> E) = Invalid<T, E>(es)
        }

    override fun toString() = "${javaClass.simpleName}[${es.joinToString(" -> ")}}]"
}

