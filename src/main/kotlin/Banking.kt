package org.example

import org.example.Refinement.Companion.validate
import org.example.Currency.Companion.times
import org.example.Currency.Companion.plus

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
        fun refinement(): Refinement<L<Long>, Positive> =
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

interface Currency : NonNegative {
    companion object {
        fun refinement(): Refinement<L<Long>, Currency> =
            object : Refinement<L<Long>, Currency> {
                override fun isValid(value: L<Long>): Boolean =
                    NonNegative.refinement().isValid(value) // No additional requirements
            }

        /**
         * Declaring those operators here allows Banking.calculateInterest
         * to work with any currency, but on the other hand, it allows
         * addition EUR + USD which results in Currency
         */

        operator fun <T> T.plus(other: T): T where T : L<Long>, T : Currency =
            L.lift(this.value + other.value) as T

        operator fun <T, P> T.times(other: P): T where T : L<Long>, T : Currency, P : L<Long>, P : Percentage =
            L.lift(this.value * (other.value / 100L)) as T
    }
}

interface EUR : Currency {
    companion object {
        fun refinement(): Refinement<L<Long>, EUR> =
            object : Refinement<L<Long>, EUR> {
                override fun isValid(value: L<Long>): Boolean =
                    Currency.refinement().isValid(value) // No additional requirements
            }

        operator fun <T> T.plus(other: T): T where T : L<Long>, T : EUR =
            L.lift(this.value + other.value) as T
    }
}

interface USD : Currency {
    companion object {
        fun refinement(): Refinement<L<Long>, USD> =
            object : Refinement<L<Long>, USD> {
                override fun isValid(value: L<Long>): Boolean =
                    Currency.refinement().isValid(value) // No additional requirements
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
    // Order of bounds is important to avoid runtime checks generation by compiler
            where I : L<String>, I : IBAN, E : L<Long>, E : EUR = L.lift(100L) as E

    fun <I, E> getUSDBalance(account: I): E
            where I : L<String>, I : IBAN, E : L<Long>, E : USD = L.lift(100L) as E

    fun <I, E> transferEUR(from: I, to: I, amount: E): Unit
            where I : L<String>, I : IBAN, E : L<Long>, E : EUR {
        println("Transferred $amount EUR from $from to $to")
    }

    fun <I, E> transferUSD(from: I, to: I, amount: E): Unit
            where I : L<String>, I : IBAN, E : L<Long>, E : USD {
        println("Transferred $amount USD from $from to $to")
    }

    fun <T, I, Y> calculateInterest(balance: T, rate: I, years: Y): T
            where T : L<Long>, T : Currency,
                  I : L<Long>, I : Percentage,
                  Y : L<Long>, Y : Positive {
        // refinement is not lost in the process
        var current = balance
        repeat(years.value.toInt()) {
            val interest = current * rate
            current = current + interest
        }

        return current
    }
}

fun main() {
    // Does not compile
//        Banking.transferEUR(L.lift("test"), L.lift("test"), L.lift(0L))

    val from = L.lift("NL39RABO03005320135117")
    val to = L.lift("NL13ABNA0417164300")
    if (IBAN.refinement().validate(from) && IBAN.refinement().validate(to)) {
        // Can't store balance in val bc type could not be deduced or written explicitly
        Banking.transferEUR(from, to, Banking.getEURBalance(from))
    }

    val bankAccount = L.lift("NL13ABNA0417164300")
    val account = L.lift("NL39RABO03005320135117")
    val rate = L.lift(10L)
    val years = L.lift(5L)
    if (IBAN.refinement().validate(bankAccount) &&
        IBAN.refinement().validate(account) &&
        Percentage.refinement().validate(rate) &&
        Positive.refinement().validate(years)
    ) {
        // Can store the result in a val bc type is deduced from argument
        val eurInterest = Banking.calculateInterest(
            Banking.getEURBalance(account),
            rate,
            years
        )

        Banking.transferEUR(account, bankAccount, eurInterest)
        // Does not compile
        //Banking.transferUSD(account, bankAccount, eurInterest)

        val usdInterest = Banking.calculateInterest(
            Banking.getUSDBalance(account),
            rate,
            years
        )

        Banking.transferUSD(account, bankAccount, usdInterest)

        // Correctly results in eur
        val sumEur = eurInterest + eurInterest
        // Sadly does not result in an error, but what can you do with currency?
        val sumCurrency = eurInterest + usdInterest
    }
}
