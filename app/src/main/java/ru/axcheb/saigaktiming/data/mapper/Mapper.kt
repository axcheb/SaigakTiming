package ru.axcheb.saigaktiming.data.mapper

interface Mapper<I, O> {
    fun map(input: I): O
}