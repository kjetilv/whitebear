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
 * @param E The error type
 */
interface ErrorProcessor<E> {

    /**
     * What does the empty aggegrated type look like?
     */
    val empty: E

    /**
     * How do we combine two aggregates into one?
     */
    fun combine(error1: E, error2: E): E
}

