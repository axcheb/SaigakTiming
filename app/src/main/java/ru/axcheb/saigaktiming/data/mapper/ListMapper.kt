package ru.axcheb.saigaktiming.data.mapper

class ListMapper<I, O>(private val mapper: Mapper<I, O>) : Mapper<List<I>, List<O>> {
    override fun map(input: List<I>): List<O> {
        return input.map { mapper.map(it) }
    }
}