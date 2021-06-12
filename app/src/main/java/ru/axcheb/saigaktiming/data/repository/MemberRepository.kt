package ru.axcheb.saigaktiming.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.axcheb.saigaktiming.data.dao.MemberDao
import ru.axcheb.saigaktiming.data.mapper.EventMemberItemMapper
import ru.axcheb.saigaktiming.data.mapper.ListMapper
import ru.axcheb.saigaktiming.data.model.db.Event
import ru.axcheb.saigaktiming.data.model.db.EventMemberCrossRef
import ru.axcheb.saigaktiming.data.model.db.Member
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem

class MemberRepository(private val memberDao: MemberDao) {

    private val listOfEventMemberItemMapper = ListMapper(EventMemberItemMapper())

    fun getMemberSelected(eventId: Long): Flow<List<MemberSelectItem>> {
        return memberDao.getMemberSelectItems(eventId)
    }

    fun getMember(id: Long) = memberDao.getMember(id)

    fun getMember(name: String) = memberDao.getMember(name)

    fun getEventMember(eventId: Long, memberId: Long) = memberDao.getEventMember(eventId, memberId)

    suspend fun updateEventMemberCrossRef(eventMemberCrossRef: EventMemberCrossRef) =
        memberDao.updateEventMemberCrossRef(eventMemberCrossRef)

    suspend fun insert(member: Member) = memberDao.insert(member)

    suspend fun saveNewAndBind(member: Member, eventId: Long) {
        memberDao.insertAndBindToEvent(member, eventId)
    }

    suspend fun bindToEvent(eventId: Long, memberId: Long) =
        memberDao.bindToEvent(eventId, memberId)

    suspend fun subtractSequenceNumberAndUnbind(
        eventId: Long,
        sequenceNumber: Int,
        memberId: Long
    ) = memberDao.subtractSequenceNumberAndUnbind(eventId, sequenceNumber, memberId)

    fun getEventMemberItems(event: Event): Flow<List<EventMemberItem>> {
        val eventId = event.id ?: return flowOf(emptyList())
        return memberDao.getEventMemberCrossRefAndMemberSorted(eventId).map {
            it.map { em -> Triple(em, event, it.size) }
        }.map { listOfEventMemberItemMapper.map(it) }
    }

    fun getMembers(eventId: Long) = memberDao.getMembers(eventId)

}