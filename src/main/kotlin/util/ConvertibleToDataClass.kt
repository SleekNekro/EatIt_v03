package com.github.SleekNekro.util

fun interface ConvertibleToDataClass<T> {
    fun toDataClass(): T
}