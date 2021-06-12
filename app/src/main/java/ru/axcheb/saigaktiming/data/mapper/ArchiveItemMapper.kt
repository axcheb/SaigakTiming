package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.Event
import ru.axcheb.saigaktiming.data.model.ui.ArchiveItem

class ArchiveItemMapper : Mapper<Event, ArchiveItem> {
    override fun map(input: Event): ArchiveItem {
        return ArchiveItem(input.id!!, input.date)
    }
}