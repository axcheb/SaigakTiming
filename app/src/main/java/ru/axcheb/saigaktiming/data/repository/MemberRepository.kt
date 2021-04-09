package ru.axcheb.saigaktiming.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.data.dao.MemberDao
import ru.axcheb.saigaktiming.data.model.domain.Member
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem

class MemberRepository(private val memberDao: MemberDao) {

    fun getMemberSelected(eventId: Long): Flow<List<MemberSelectItem>> {
        return memberDao.getMemberSelectItems(eventId)
        // TODO оставил для примера
//        val membersFlow = memberDao.getMembers()
//        return eventMembersFlow.map { it.map { em -> em.id to em.sequenceNumber }.toMap() }.combine(membersFlow) { memberMap, members ->
//            members.map { MemberSelectItem(member = it, memberMap.contains(it.id)) }
//        }
    }

    fun getMember(id: Long) = memberDao.getMember(id)

    fun getMember(name: String) = memberDao.getMember(name)

    fun getEventMember(eventId: Long, memberId: Long) = memberDao.getEventMember(eventId, memberId)

    suspend fun insert(member: Member) = memberDao.insert(member)

    @Transaction
    suspend fun saveNewAndBind(member: Member, eventId: Long) {
        val memberId = memberDao.insert(member)
        bindToEvent(eventId, memberId)
    }

    @Transaction
    suspend fun handleEventMemberBind(eventId: Long, memberId: Long) {
        val eventMember = memberDao.getEventMember(eventId, memberId).firstOrNull()
        if (eventMember == null) {
            bindToEvent(eventId, memberId)
        } else {
            memberDao.subtractSequenceNumber(eventId, eventMember.sequenceNumber)
            unBindToEvent(eventId, memberId)
        }
    }

    suspend fun bindToEvent(eventId: Long, memberId: Long) = memberDao.bindToEvent(eventId, memberId)

    suspend fun unBindToEvent(eventId: Long, memberId: Long) = memberDao.unBindToEvent(eventId, memberId)

    fun getEventMemberItems(eventId: Long) = memberDao.getEventMemberItems(eventId)

    fun getMembers(eventId: Long) = memberDao.getMembers(eventId)

}