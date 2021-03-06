package ru.axcheb.saigaktiming.ui

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.axcheb.saigaktiming.data.model.ui.*
import ru.axcheb.saigaktiming.ui.archive.ArchiveAdapter
import ru.axcheb.saigaktiming.ui.archive.ArchiveViewModel
import ru.axcheb.saigaktiming.ui.event.EditEventViewModel
import ru.axcheb.saigaktiming.ui.event.EventMemberAdapter
import ru.axcheb.saigaktiming.ui.event.EventViewModel
import ru.axcheb.saigaktiming.ui.start.StartAdapter
import ru.axcheb.saigaktiming.ui.start.StartViewModel
import ru.axcheb.saigaktiming.ui.memberselect.*
import ru.axcheb.saigaktiming.ui.finish.FinishAdapter
import ru.axcheb.saigaktiming.ui.finish.FinishViewModel
import ru.axcheb.saigaktiming.ui.protocol.ProtocolAdapter
import ru.axcheb.saigaktiming.ui.protocol.ProtocolViewModel
import ru.axcheb.saigaktiming.ui.sensors.SensorsViewModel

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
            eventRepository = get(),
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
            eventRepository = get(),
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
            eventRepository = get(),
            settingsRepository = get(),
            application = get()
        )
    }
    viewModel {
        EditEventViewModel(eventRepository = get())
    }
    viewModel { (eventId: Long) ->
        ProtocolViewModel(
            eventId = eventId,
            resultRepository = get()
        )
    }
    viewModel {
        ArchiveViewModel(eventRepository = get())
    }
    viewModel {
        SensorsViewModel(settingsRepository = get())
    }

    factory { (bindMemberListener: (MemberSelectItem) -> Unit) ->
        MemberSelectAdapter(bindMemberListener)
    }
    factory {
        EventMemberAdapter()
    }
    factory { (eventId: Long, memberId: Long, startActiveListener: (StartItem) -> Unit) ->
        StartAdapter(eventId, memberId, startActiveListener)
    }
    factory { (finishActiveListener: (FinishItem) -> Unit) ->
        FinishAdapter(finishActiveListener)
    }
    factory {
        ProtocolAdapter()
    }
    factory { (itemClickListener: (ArchiveItem) -> Unit) ->
        ArchiveAdapter(itemClickListener)
    }

}