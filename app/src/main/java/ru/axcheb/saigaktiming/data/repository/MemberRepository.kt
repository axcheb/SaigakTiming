package ru.axcheb.saigaktiming.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import ru.axcheb.saigaktiming.data.dao.MemberDao
import ru.axcheb.saigaktiming.data.model.domain.Member
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem

class MemberRepository(private val memberDao: MemberDao) {

    fun getMemberSelected(eventId: Long): Flow<List<MemberSelectItem>> {
        return memberDao.getMemberSelectItems(eventId)
    }

    fun getMember(id: Long) = memberDao.getMember(id)

    fun getMember(name: String) = memberDao.getMember(name)

    fun getEventMember(eventId: Long, memberId: Long) = memberDao.getEventMember(eventId, memberId)

    suspend fun insert(member: Member) = memberDao.insert(member)

    suspend fun saveNewAndBind(member: Member, eventId: Long) {
        memberDao.insertAndBindToEvent(member, eventId)
    }

    suspend fun handleEventMemberBind(eventId: Long, memberId: Long) {
        val eventMember = memberDao.getEventMember(eventId, memberId).firstOrNull()
        if (eventMember == null) {
            memberDao.bindToEvent(eventId, memberId)
        } else {
            memberDao.subtractSequenceNumberAndUnbind(eventId, eventMember.sequenceNumber, memberId)
        }
    }

    fun getEventMemberItems(eventId: Long) = memberDao.getEventMemberItems(eventId)

    fun getMembers(eventId: Long) = memberDao.getMembers(eventId)

}