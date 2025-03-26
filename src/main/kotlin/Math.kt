package org.example

import org.example.Refinement.Companion.validate

interface NonNeg {
    companion object {
        fun refinement(): Refinement<L<Long>, NonNeg> =
            object : Refinement<L<Long>, NonNeg> {
                override fun isValid(value: L<Long>): Boolean =
                    value.value >= 0L
            }
    }
}

interface NonPos {
    companion object {
        fun refinement(): Refinement<L<Long>, NonPos> =
            object : Refinement<L<Long>, NonPos> {
                override fun isValid(value: L<Long>): Boolean =
                    value.value <= 0L
            }
    }
}

fun <T> T.meaning(): Int where T : L<Long>, T : NonNeg = 42

fun <T> T.meaning(): Int where T : L<Long>, T : NonPos = -42

fun main() {
    val v = L.lift(10L)
    if (NonNeg.refinement().validate(v) && NonPos.refinement().validate(v)) {
        // Does not compile
        //println(v.meaning())
    }
}

