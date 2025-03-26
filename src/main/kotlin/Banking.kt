package org.example

import org.example.Refinement.Companion.validate

interface NonNegative {
    companion object {
        fun refinement(): Refinement<L<Long>, NonNegative> =
            object : Refinement<L<Long>, NonNegative> {
                override fun isValid(value: L<Long>): Boolean =
                    value.value >= 0L
            }
    }
}

interface Positive : NonNegative {
    companion object {
        fun <T> refinement(): Refinement<L<Long>, Positive>
                where T : Number, T : Comparable<T> =
            object : Refinement<L<Long>, Positive> {
                override fun isValid(value: L<Long>): Boolean =
                    value.value > 0L
            }
    }
}

interface Percentage : NonNegative {
    companion object {
        fun refinement(): Refinement<L<Long>, Percentage> =
            object : Refinement<L<Long>, Percentage> {
                override fun isValid(value: L<Long>): Boolean =
                    value.value in 0..100L
            }
    }
}

interface Money : NonNegative {
    companion object {
        fun refinement(): Refinement<L<Long>, Money> =
            object : Refinement<L<Long>, Money> {
                override fun isValid(value: L<Long>): Boolean =
                    NonNegative.refinement().isValid(value) // No additional requirements
            }
    }
}

interface EUR : Money {
    companion object {
        fun refinement(): Refinement<L<Long>, EUR> =
            object : Refinement<L<Long>, EUR> {
                override fun isValid(value: L<Long>): Boolean =
                    Money.refinement().isValid(value) // No additional requirements
            }
    }
}

interface USD : Money {
    companion object {
        fun refinement(): Refinement<L<Long>, USD> =
            object : Refinement<L<Long>, USD> {
                override fun isValid(value: L<Long>): Boolean =
                    Money.refinement().isValid(value) // No additional requirements
            }
    }
}

interface IBAN {
    companion object {
        fun refinement(): Refinement<L<String>, IBAN> =
            object : Refinement<L<String>, IBAN> {
                override fun isValid(value: L<String>): Boolean =
                    value.value.matches(Regex("^[A-Z]{2}[0-9]{2}[0-9A-Za-z]{11,28}$"))
            }
    }
}

object Banking {
    fun <I, E> getEURBalance(account: I): E
            where I : L<String>, I : IBAN, E : L<Long>, E : EUR = L.lift(100L) as E

    fun <I, E> transferEUR(from: I, to: I, amount: E): Unit
            where I : L<String>, I : IBAN, E : L<Long>, E : EUR {
        println("Transferred $amount EUR from $from to $to")
    }
}

fun main() {
    // Does not compile
//        Banking.transferEUR(L.lift("test"), L.lift("test"), L.lift(0L))

    val from = L.lift("NL39RABO03005320135117")
    val to = L.lift("NL13ABNA0417164300")
    val amount = L.lift(100L)
    if (IBAN.refinement().validate(from) && IBAN.refinement().validate(to) && EUR.refinement().validate(amount)) {
        Banking.transferEUR(from, to, Banking.getEURBalance(from))
    }
}
