package com.mmutert.trackmydebt.data

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[$data]"
            is Error -> "Error[$exception]"
        }
    }
}
