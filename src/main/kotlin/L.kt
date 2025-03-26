package org.example

// Can't make intersection with non-interface, so use this
// `L` stands for lifted
interface L<T> {
    val value: T

    companion object {
        fun <T> lift(value: T): L<T> = object : L<T> {
            override val value: T = value

            override fun toString(): String = value.toString()
        }
    }
}