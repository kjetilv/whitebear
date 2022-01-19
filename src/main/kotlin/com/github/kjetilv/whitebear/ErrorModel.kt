package com.github.kjetilv.whitebear

interface ErrorModel<E, A> {

    val empty: A

    infix fun isEmpty(aggregator: A): Boolean = aggregator == empty

    fun combine(aggregator1: A, aggregator2: A): A

    fun add(aggregator: A, error: E): A

    infix fun str(aggregator: A) = "$aggregator"

    infix fun singleton(e: E) = add(empty, e)
}

interface SimpleErrorModel<E> : ErrorModel<E, E> {

    override fun add(aggregator: E, error: E): E = combine(aggregator, error)
}
