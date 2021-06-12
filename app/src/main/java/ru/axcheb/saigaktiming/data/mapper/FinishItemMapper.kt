package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.FinishItemDb
import ru.axcheb.saigaktiming.data.model.ui.FinishItem

class FinishItemMapper: Mapper<FinishItemDb, FinishItem> {
    override fun map(input: FinishItemDb): FinishItem {
        return FinishItem(input.id, input.sensor, input.startTime, input.finishTime, input.isActive)
    }
}