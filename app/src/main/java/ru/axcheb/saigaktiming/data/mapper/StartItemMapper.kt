package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.StartItemDb
import ru.axcheb.saigaktiming.data.model.ui.StartItem

class StartItemMapper: Mapper<StartItemDb, StartItem> {
    override fun map(input: StartItemDb): StartItem {
        return StartItem(input.id, input.sensor, input.startTime, input.finishTime, input.isActive)
    }
}