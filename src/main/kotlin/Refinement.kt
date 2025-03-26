package org.example

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

interface Refinement<T, R> {
    fun isValid(value: T): Boolean

    companion object {
        @OptIn(ExperimentalContracts::class)
        inline fun <T, reified R> Refinement<T, R>.validate(value: T): Boolean {
            contract {
                returns(true) implies (value is R)
            }

            return isValid(value)
        }
    }
}

