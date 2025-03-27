package com.github.SleekNekro.dao

fun interface ConvertibleToDataClass<T> {
    fun toDataClass(): T
}