package ru.axcheb.saigaktiming.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.model.dto.EventMemberCrossRef
import ru.axcheb.saigaktiming.data.model.dto.Member
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem

@Dao
interface MemberDao {

    @Query("select * from member order by name")
    fun getMembers(): Flow<List<Member>>

    @Query("select * from member where id = :id")
    fun getMember(id: Long): Flow<Member>

    @Query("select * from member where name = :name")
    fun getMember(name: String): Flow<Member>

    @Insert
    suspend fun insert(member: Member): Long

    @Query("""
        select  
            member.id as member_id,
            (
                select sequence_number 
                from event_member where event_id = :eventId and event_member.member_id = member.id
            ) as sequence_number,  
            name
        from  member
        order by member.name
        """)
    fun getMemberSelectItems(eventId: Long): Flow<List<MemberSelectItem>>

    @Query("""
        insert into event_member(event_id, member_id, sequence_number) 
        values (:eventId, :memberId, 
            (select count(*) from event_member where event_id = :eventId) + 1)
        """)
    suspend fun bindToEvent(eventId: Long, memberId: Long)

    @Query("delete from event_member where event_id = :eventId and member_id = :memberId")
    suspend fun unBindToEvent(eventId: Long, memberId: Long)

    @Query("select * from event_member where event_id = :eventId and member_id = :memberId")
    fun getEventMember(eventId: Long, memberId: Long): Flow<EventMemberCrossRef>

    @Update
    suspend fun updateEventMemberCrossRef(eventMemberCrossRef: EventMemberCrossRef)

    @Query("""
        update event_member 
            set sequence_number = sequence_number - 1
        where 
            event_id = :eventId
            and sequence_number > :sequenceNumber
         
    """)
    suspend fun subtractSequenceNumber(eventId: Long, sequenceNumber: Int)

    @Query("""
        select 
            event_member.id as id, 
            event_id, 
            member_id, 
            sequence_number, 
            name 
        from event_member 
            join member on event_member.member_id = member.id 
        where event_id = :eventId
        order by sequence_number
        """)
    fun getEventMemberItems(eventId: Long): Flow<List<EventMemberItem>>

    @Query("""
        select 
            member.id as id
            ,member.name
        from member
            join event_member on event_member.member_id = member.id
        where event_id = :eventId
        order by sequence_number
    """)
    fun getMembers(eventId: Long): Flow<List<Member>>

    @Transaction
    suspend fun subtractSequenceNumberAndUnbind(eventId: Long, sequenceNumber: Int, memberId: Long) {
        subtractSequenceNumber(eventId, sequenceNumber)
        unBindToEvent(eventId, memberId)
    }

    @Transaction
    suspend fun insertAndBindToEvent(member: Member, eventId: Long): Long {
        val memberId = insert(member)
        bindToEvent(eventId, memberId)
        return memberId
    }

}