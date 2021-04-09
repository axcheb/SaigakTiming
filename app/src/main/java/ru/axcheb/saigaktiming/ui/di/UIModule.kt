package ru.axcheb.saigaktiming.ui

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.ui.event.EventMemberAdapter
import ru.axcheb.saigaktiming.ui.event.EventViewModel
import ru.axcheb.saigaktiming.ui.start.StartAdapter
import ru.axcheb.saigaktiming.ui.start.StartViewModel
import ru.axcheb.saigaktiming.ui.memberselect.*
import ru.axcheb.saigaktiming.ui.finish.FinishAdapter
import ru.axcheb.saigaktiming.ui.finish.FinishViewModel

val uiModule = module {
    viewModel {
        EventViewModel(
            memberRepository = get(),
            eventRepository = get()
        )
    }
    viewModel { (eventId: Long) ->
        MemberSelectViewModel(
            memberRepository = get(),
            eventId = eventId
        )
    }
    viewModel { (eventId: Long) ->
        NewMemberViewModel(
            memberRepository = get(),
            eventId = eventId
        )
    }

    viewModel { (eventId: Long, memberId: Long) ->
        StartViewModel(
            eventId = eventId,
            memberId = memberId,
            memberRepository = get(),
            resultRepository = get(),
        )
    }
    viewModel { (eventId: Long, memberId: Long, startId: Long) ->
        FinishViewModel(
            eventId = eventId,
            memberId = memberId,
            startId = startId,
            memberRepository = get(),
            resultRepository = get(),
            application = get()
        )
    }

    factory { (bindMemberListener: (MemberSelectItem) -> Unit) ->
        MemberSelectAdapter(bindMemberListener)
    }
    factory {
        EventMemberAdapter()
    }
    factory { (eventId: Long, memberId: Long, startActiveListener: (ResultItem) -> Unit) ->
        StartAdapter(eventId, memberId, startActiveListener)
    }
    factory { (finishActiveListener: (ResultItem) -> Unit) ->
        FinishAdapter(finishActiveListener)
    }

}