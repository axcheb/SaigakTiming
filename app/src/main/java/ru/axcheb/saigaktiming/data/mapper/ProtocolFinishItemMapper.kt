package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.StartAndFinish
import ru.axcheb.saigaktiming.data.model.ui.ProtocolFinishItem

class ProtocolFinishItemMapper : Mapper<StartAndFinish, ProtocolFinishItem> {
    override fun map(input: StartAndFinish): ProtocolFinishItem {
        return ProtocolFinishItem(
            input.start.id!!,
            input.finish.id!!,
            input.finish.sensorId,
            input.finish.time.time - input.start.time.time
        )
    }
}