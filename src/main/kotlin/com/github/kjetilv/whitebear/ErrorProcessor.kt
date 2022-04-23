package com.github.kjetilv.whitebear

/**
 * An error model lets us describe to the validation mechanism how we want to represent the validation errors.
 * <P>
 * It describes two things: The validation type V, representing a single validation,
 * and the aggregate validation type A, representing a combined view of potentially
 * several instances of V instances
 * <P>
 * As an example, consider {@link String} as the validation type, representing an error message, and
 * {@link List} of strings as the aggregate, representing several.
 *
 * @param E The type for a single validation
 * @param A The aggregate os several validations.
 */
interface ErrorProcessor<E, A> {

    /**
     * What does the empty aggegrated type look like?
     */
    val empty: A

    /**
     * How do we combine two aggregates into one?
     */
    fun combine(aggregator1: A, aggregator2: A): A

    infix fun wrap(error: E): A

    /**
     * Is this aggregator empty? Suggested implementation: Equal to {@link #empty}.
     */
    infix fun isEmpty(aggregator: A): Boolean = aggregator == empty

    infix fun isNotEmpty(aggregator: A): Boolean = !isEmpty(aggregator)

    /**
     * How do we add a validation error to the aggregate?
     */
    fun add(aggregator: A, error: E): A = combine(aggregator, wrap(error))

    /**
     * How can we print the aggregate?  Suggested implementation: Its {@link Object#toString}.
     */
    infix fun str(aggregator: A) = "$aggregator"
}

/**
 * A simple {@link ErrorModel error model} can be used when the validation type is the same as the
 * aggregate, eg. when a {@link String} represents a validation failure and a {@link String} also represents
 * the aggregate, created by concatenating strings.
 */
interface SimpleErrorProcessor<E> : ErrorProcessor<E, E> {

    override fun wrap(error: E) = error
}
