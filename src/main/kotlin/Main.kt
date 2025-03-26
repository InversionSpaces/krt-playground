package org.example

import org.example.NonEmptyList.Companion.safeMin
import org.example.Refinement.Companion.validate

interface NonEmptyList<T> {
    companion object {
        fun <T: Comparable<T>, U> U.safeMin() where U: List<T>, U : NonEmptyList<T> =
            this.minOrNull()!!

        fun <T> refinement() : Refinement<List<T>, NonEmptyList<T>> =
            object : Refinement<List<T>, NonEmptyList<T>> {
                override fun isValid(value: List<T>): Boolean =
                    value.isNotEmpty()
            }
    }
}

interface DoubleNonEmptyList<T> : NonEmptyList<T> {
    companion object {
        fun <T, U> U.safeFirstAndSecond() where U : List<T>, U : DoubleNonEmptyList<T> =
            (this.first() to this[1])

        fun <T> refinement() : Refinement<List<T>, DoubleNonEmptyList<T>> =
            object : Refinement<List<T>, DoubleNonEmptyList<T>> {
                override fun isValid(value: List<T>): Boolean =
                    value.size > 1
            }
    }
}

fun <T: Comparable<T>, U> useNonEmptyList(list: U) where U : List<T>, U: NonEmptyList<T> =
    list.safeMin()

/**
 * class B { fun foo() }
 * class A : B
 *
 * ref RB
 * ref RA: RB { fun foo() }
 *
 * val a: A with RA
 * a.foo()
 */
fun main() {

    val list = listOf(1, 2, 3)
//
//    // Does not compile
//    println(list.safeMin())
//
    if (NonEmptyList.refinement<Int>().validate(list)) {
//        println(list.safeMin())
        println(useNonEmptyList(list))
    }
//
//    if (DoubleNonEmptyList.refinement<Int>().validate(list)) {
//        println(list.safeFirstAndSecond())
//        println(useNonEmptyList(list))
//    }

//    val test: Int = null
//    if (test is String) {
//        println(test)
//    }
}