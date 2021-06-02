package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.dto.EventMemberCrossRefAndMember
import ru.axcheb.saigaktiming.data.model.dto.StartAndFinish
import ru.axcheb.saigaktiming.data.model.ui.ProtocolItem

class ListOfProtocolItemMapper :
    Mapper<Pair<List<EventMemberCrossRefAndMember>, List<StartAndFinish>>, List<ProtocolItem>> {

    private val listOfProtocolFinishItemMapper = ListMapper(ProtocolFinishItemMapper())

    override fun map(input: Pair<List<EventMemberCrossRefAndMember>, List<StartAndFinish>>): List<ProtocolItem> {
        val (eventMembers, startAndFinishList) = input
        val startAndFinishMap = startAndFinishList.groupBy { it.start.eventMemberId }
        return eventMembers.map {
            val memberStartAndFinishList =
                startAndFinishMap.getOrDefault(it.eventMemberCrossRef.id, emptyList())
            ProtocolItem(
                it.eventMemberCrossRef.id!!,
                it.eventMemberCrossRef.eventId!!,
                it.eventMemberCrossRef.sequenceNumber,
                it.eventMemberCrossRef.penaltySeconds,
                it.member.id!!,
                it.member.name,
                calcResultMillis(memberStartAndFinishList, it.eventMemberCrossRef.penaltySeconds),
                listOfProtocolFinishItemMapper.map(memberStartAndFinishList)
            )
            // у кого больше заездов, тот выше, затем учитывается время
        }.sortedWith(compareBy({ -it.trackResults.size }, { it.resultMillis }))
            .mapIndexed { index: Int, pi: ProtocolItem ->
                pi.position = index + 1
                pi
            }
    }

    private fun calcResultMillis(
        startAndFinishList: List<StartAndFinish>,
        penaltySeconds: Int
    ): Long {
        return penaltySeconds * 1_000L + startAndFinishList.map { it.finish.time.time - it.start.time.time }
            .sum()
    }
}