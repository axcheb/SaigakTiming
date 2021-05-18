package ru.axcheb.saigaktiming.data.repository

import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.dao.MemberDao
import ru.axcheb.saigaktiming.data.model.dto.EventMemberCrossRef
import ru.axcheb.saigaktiming.data.model.dto.Member
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem

class MemberRepository(private val memberDao: MemberDao) {

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
    ) =
        memberDao.subtractSequenceNumberAndUnbind(eventId, sequenceNumber, memberId)

    fun getEventMemberItems(eventId: Long) = memberDao.getEventMemberItems(eventId)

    fun getMembers(eventId: Long) = memberDao.getMembers(eventId)

}