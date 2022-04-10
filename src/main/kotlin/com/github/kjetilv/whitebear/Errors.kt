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
 * @param V The type for a single validation
 * @param A The aggregate os several validations.
 */
interface Errors<V, A> {

    /**
     * What does the empty aggegrated type look like?
     */
    val empty: A

    /**
     * Is this aggregator empty? Suggested implementation: Equal to {@link #empty}.
     */
    infix fun isEmpty(aggregator: A): Boolean = aggregator == empty

    infix fun isNotEmpty(aggregator: A): Boolean = !isEmpty(aggregator)

    /**
     * How do we combine two aggregates into one?
     */
    fun combine(aggregator1: A, aggregator2: A): A

    /**
     * How do we add a validation error to the aggregate?
     */
    fun add(aggregator: A, error: V): A

    /**
     * How can we print the aggregate?  Suggested implementation: Its {@link Object#toString}.
     */
    infix fun str(aggregator: A) = "$aggregator"

    /**
     * What does an aggregate with a single validation error look like?  Suggested implementation:
     * {@link #add(A, V) add} it to {@link #empty}.
     */
    infix fun singleton(e: V) = add(empty, e)
}

/**
 * A simple {@link ErrorModel error model} can be used when the validation type is the same as the
 * aggregate, eg. when a {@link String} represents a validation failure and a {@link String} also represents
 * the aggregate, created by concatenating strings.
 */
interface SimpleErrors<E> : Errors<E, E> {

    /**
     * In a simple error model, adding aggregates is the same as combining them.
     */
    override fun add(aggregator: E, error: E): E = combine(aggregator, error)
}
