package ru.axcheb.saigaktiming.ui.finish

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.*
import ru.axcheb.saigaktiming.data.mapper.SensorMessageMapper
import ru.axcheb.saigaktiming.data.model.dto.*
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository
import ru.axcheb.saigaktiming.data.repository.SettingsRepository
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService
import java.util.*

class FinishViewModel(
    private val eventId: Long,
    private val memberId: Long,
    private val startId: Long,
    private val memberRepository: MemberRepository,
    private val resultRepository: ResultRepository,
    private val eventRepository: EventRepository,
    settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    /**
     * Flow событий срабатывания фотодатчика финиша.
     */
    private val _finishFlow = MutableSharedFlow<SensorMessage>(1, 0, BufferOverflow.DROP_OLDEST)

    private val _status = MutableStateFlow(Status.PAUSED)

    private val isImitationMode = settingsRepository.get().map { it.isImitationMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isTimerCardVisible =
        _status.map { it != Status.VIEW_ONLY }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _millisFromStartCountingFlow = MutableStateFlow(0L)

    val timerStr
        get() = combine(_millisFromStartCountingFlow, _status) { millisFromStartCounting, status ->
            formatToTimeStr(millisFromStartCounting, status)
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private var beforeStartTime = BEFORE_START_TIME

    private val sensorMessageMapper = SensorMessageMapper()

    private fun formatToTimeStr(millisFromStartCounting: Long, status: Status): String {
        return when (status) {
            Status.WAITING_START -> {
                ((millisFromStartCounting / ONE_SECOND * ONE_SECOND - beforeStartTime) / ONE_SECOND).toString()
            }
            Status.STARTED_WAITING_FINISH -> {
                val waitingFinishSeconds =
                    (millisFromStartCounting - beforeStartTime) / ONE_SECOND
                if (waitingFinishSeconds == 0L) {
                    application.resources.getString(R.string.start_uppercase)
                } else {
                    waitingFinishSeconds.formatElapsedTimeSeconds()
                }
            }
            Status.FINISHED -> {
                application.resources.getString(R.string.time_over)
            }
            Status.VIEW_ONLY -> {
                ""
            }
            Status.PAUSED -> {
                application.resources.getString(R.string.pause)
            }
        }
    }

    private lateinit var event: Event

    private lateinit var originalMembers: List<Member>

    /**
     * Список участников, которые выйдут на старт.
     */
    private var members: MutableList<Member> = arrayListOf()

    /**
     * Текущий выбранный участник.
     */
    private val _member: MutableStateFlow<Member?> = MutableStateFlow(null)
    val member: StateFlow<Member?> = _member

    /**
     * Следующий участник.
     */
    private val _nextMember: MutableStateFlow<Member?> = MutableStateFlow(null)
    val nextMember: StateFlow<Member?> = _nextMember

    private var timerJob: Job? = null
    private var membersForEachJob: Job? = null

    private val _startIdFlow = MutableStateFlow(startId)
    private val start: SharedFlow<Start?> =
        _startIdFlow.flatMapLatest { startId -> resultRepository.getStart(startId) }
            .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    val startTimeStr =
        start.map { start -> start?.time?.hhmmsssssStr() ?: "" }
            .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val finishItems: StateFlow<List<ResultItem>> =
        _startIdFlow.flatMapLatest { startId -> resultRepository.getFinishResults(startId) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isNextEnabled: StateFlow<Boolean> =
        _status.map {
            (it == Status.WAITING_START || it == Status.STARTED_WAITING_FINISH)
                    // Кнопка Next доступна для всех участников на СУ, кроме последнего. Его пропускать нельзя.
                    && (event.currentMemberIndex + 1 < members.count())
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isPauseEnabled: StateFlow<Boolean> =
        _status.map {
            it == Status.WAITING_START
                    || it == Status.STARTED_WAITING_FINISH
                    || it == Status.FINISHED
        }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val pauseOrResume: StateFlow<Boolean> =
        _status.map { it != Status.PAUSED }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isToEndEnabled: StateFlow<Boolean> = _status.map { it == Status.WAITING_START }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    /**
     * Можно ли взаимодействовать с таймером: запускать, приостанавливать и т.д.
     */
    val isTimerRunnable: StateFlow<Boolean> = _status.map { it != Status.VIEW_ONLY }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    /** Работа происходит только с одним конкретным участником без учёта currentMemberIndex и т.п. */
    private val isWorkWithOneMember = memberId != NULL_ID

    /** Был ли уже запущен этот единственные участник. */
    private var isOneMemberLaunched = false

    /** Поменялся ли СУ в процессе в процессе работы для участника [Event.currentMemberIndex]. */
    private var isTrackChanged = false

    init {
        viewModelScope.launch {
            event = eventRepository.getEvent(eventId).first()
            if (startId != NULL_ID) {  // Если startId задан, вьюха открывается только на просмотр.
                _status.value = Status.VIEW_ONLY
                _startIdFlow.value = startId
            }

            if (isWorkWithOneMember) {
                val m = memberRepository.getMember(memberId).firstOrNull()
                m?.apply {
                    originalMembers = listOf(this)
                    members = arrayListOf(this)
                }
            } else {
                val memberList = memberRepository.getMembers(eventId).firstOrNull()
                memberList?.apply {
                    originalMembers = this
                    members = this.toMutableList()
                }
            }

            if (_status.value == Status.VIEW_ONLY) {
                if (members.isNotEmpty()) {
                    _member.value = members[0]
                }
            } else {
                // Запускаю обработку всех member, что удалось загрузить в memberList
                launchMembersForEachJob()
            }
        }

        _finishFlow.onEach {
            // Если статус STARTED_WAITING_FINISH, то при каждом срабатывании датчика финиша, сохраняется финишная отметка.
            val startId = _startIdFlow.value
            if (startId != NULL_ID && _status.value == Status.STARTED_WAITING_FINISH) {
                val finish = Finish(null, startId, it.date, it.sensorId, true)
                resultRepository.insertFinishAsOnlyActiveOne(finish)
            }
        }.launchIn(viewModelScope)

        _status.onEach {
            val mId = member.value?.id
            if (startId == NULL_ID && mId != null && it == Status.STARTED_WAITING_FINISH) {
                val startDate = Date()
                val eventMember = memberRepository.getEventMember(eventId, mId).firstOrNull()
                val eventMemberId = eventMember?.id
                if (eventMemberId != null) {
                    val start = Start(null, eventMemberId, startDate, true)
                    _startIdFlow.value = resultRepository.insert(start)
                }
            }
        }.launchIn(viewModelScope)

        launchMessageListenJob()
    }

    fun newFinish(sensorId: Int) {
        if (isImitationMode.value) {
            _finishFlow.tryEmit(SensorMessage(FINISH, sensorId, Date()))
        }
    }

    private fun launchMessageListenJob() {
        viewModelScope.launch {
            BluetoothSerialBoardService.messageFlow.collect {
                val msg = sensorMessageMapper.map(it)
                if (msg != null && msg.isFinish()) {
                    _finishFlow.emit(msg)
                }
            }
        }
    }

    /**
     * Возвращает двух участников гонки: текущего и следеющего за ним.
     * Если такого участника нет, возвращает null внутри Pair.
     */
    private fun getCurrentMembers(): Pair<Member?, Member?> {
        if (isWorkWithOneMember) {
            return if (isOneMemberLaunched) {
                Pair(null, null)
            } else {
                Pair(members[0], null)
            }
        }

        val currentTrack = event.currentTrack
        val currentMemberNumber = event.currentMemberIndex
        if (currentMemberNumber >= members.count()) {
            return Pair(null, null)
        }
        val next = getNextMemberIndex(currentTrack, currentMemberNumber)
        return if (next == null) Pair(members[currentMemberNumber], null)
        else Pair(members[currentMemberNumber], members[next.second])
    }

    private fun getNextMemberIndex(currentTrack: Int, currentMemberNumber: Int): Pair<Int, Int>? {
        var memberNumber = currentMemberNumber + 1
        var trackNumber = currentTrack
        if (memberNumber >= members.count()) {
            memberNumber = 0
            trackNumber++
        }
        if (trackNumber >= event.trackCount) {
            return null
        }
        return Pair(trackNumber, memberNumber)
    }

    /** Проверяет должен ли следующий участник ехать по следующему СУ. */
    private fun isNextMemberGoesToNextTrack(): Boolean {
        val next = getNextMemberIndex(event.currentTrack, event.currentMemberIndex) ?: return false
        // Проверка, что СУ текущего и следующего участника не совпадают.
        return event.currentTrack != next.first
    }

    /**
     * Обновляет Event и значение в БД, устанавливая текущим следующего участника и СУ.
     */
    private fun updateEventMemberToNext() {
        if (isWorkWithOneMember) return
        val next = getNextMemberIndex(event.currentTrack, event.currentMemberIndex)
        val nextTrack = next?.first ?: event.currentTrack
        val nextMember = next?.second ?: members.count()
        event.currentTrack = nextTrack
        event.currentMemberIndex = nextMember
        viewModelScope.launch {
            eventRepository.update(event)
        }
    }

    private fun launchMembersForEachJob(): Job {
        val job = viewModelScope.launch {
            var members = getCurrentMembers()
            while (members.first != null) {
                _member.value = members.first
                _nextMember.value = members.second
                _startIdFlow.value = 0
                val job = launchTimerJob()
                job.join()
                members = getCurrentMembers()
            }
            _status.value = Status.VIEW_ONLY
        }
        membersForEachJob = job
        return job
    }

    private fun launchTimerJob(): Job {
        val tJob = timerJob
        if (tJob != null && tJob.isActive) {
            throw java.lang.IllegalStateException("Cant launch timer Job. Timer Job in active state.")
        }

        val isEventLaunched = event.isLaunched
        if (!isEventLaunched && !isWorkWithOneMember) {
            event.isLaunched = true
            viewModelScope.launch {
                eventRepository.update(event)
            }
        }

        val eventStartMillis = event.date.time - BEFORE_START_TIME
        val startCountingMillis = System.currentTimeMillis()

        beforeStartTime = if (!isEventLaunched && eventStartMillis > startCountingMillis)
            eventStartMillis - startCountingMillis
        else
            BEFORE_START_TIME

        Log.d(TAG, "beforeStartTime $beforeStartTime")

        // Время, отведённое на прохождение дистанции:
        val waitingFinishTime = event.getTrackTimeMillis() - BEFORE_START_TIME
        val endMillis = beforeStartTime + waitingFinishTime
        val job = viewModelScope.launch {
            var current = System.currentTimeMillis()
            var fromStartCounting = current - startCountingMillis
            try {
                onTimerStarted()
                while (fromStartCounting < endMillis) {
                    if (fromStartCounting < beforeStartTime) {
                        // Ждём старт
                        onBeforeStartTick(fromStartCounting)
                        delay(calculateDelay(startCountingMillis, ONE_SECOND))
                    } else {
                        // Считаем время со старта
                        onWaitingFinishTick(fromStartCounting)
                        delay(calculateDelay(startCountingMillis, ONE_SECOND))
                    }
                    current = System.currentTimeMillis()
                    fromStartCounting = current - startCountingMillis
                }
                onTimerFinished(fromStartCounting)
            } catch (e: CancellationException) {
                current = System.currentTimeMillis()
                fromStartCounting = current - startCountingMillis
                onTimerCanceled(fromStartCounting)
            }
        }
        timerJob = job
        return job
    }

    private fun onTimerStarted() {
        if (_status.value == Status.PAUSED || _status.value == Status.FINISHED) {
            _status.value = Status.WAITING_START
        } else {
            throw IllegalStateException("Illegal state ${_status.value}")
        }
    }

    private fun onBeforeStartTick(millisFromStartCounting: Long) {
        if (_status.value != Status.WAITING_START) {
            throw IllegalStateException("Illegal state ${_status.value}")
        }
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    private fun onWaitingFinishTick(millisFromStartCounting: Long) {
        if (_status.value == Status.WAITING_START) {
            _status.value = Status.STARTED_WAITING_FINISH
            isTrackChanged = isNextMemberGoesToNextTrack()
            updateEventMemberToNext()
        } else if (_status.value != Status.STARTED_WAITING_FINISH) {
            throw IllegalStateException("Illegal state ${_status.value}")
        }
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    private fun onTimerCanceled(millisFromStartCounting: Long) {
        _status.value = Status.PAUSED
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    private fun onTimerFinished(millisFromStartCounting: Long) {
        if (_status.value == Status.STARTED_WAITING_FINISH) {
            _status.value = Status.FINISHED
            if (isTrackChanged) {
                // При смене СУ список участников возвращается к своему исходному состоянию.
                members = originalMembers.toMutableList()
                if (event.isAutoPauseBetweenTracks) {
                    viewModelScope.launch {
                        cancelJobs()
                        _status.value = Status.PAUSED
                    }
                }
            }
        } else {
            throw IllegalStateException("Illegal state ${_status.value}")
        }
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    /**
     * Вычисляет сколько нужно подождать до следующего вызова с заданным шагом.
     */
    private fun calculateDelay(startCountingMillis: Long, step: Long): Long {
        val current = System.currentTimeMillis()
        val prevStepMillis = (current - startCountingMillis) / step * step + startCountingMillis
        return prevStepMillis + step - current
    }

    fun onPause() {
        if (!isPauseEnabled.value) return
        if (!pauseOrResume.value) return

        viewModelScope.launch {
            if (_status.value == Status.WAITING_START || _status.value == Status.STARTED_WAITING_FINISH || _status.value == Status.FINISHED) {
                cancelJobs()
            }
        }
    }

    /**
     * Запускает таймер после паузы.
     */
    fun onResume() {
        if (pauseOrResume.value) return

        val timerJob0 = timerJob
        val membersForEachJob0 = membersForEachJob
        if (timerJob0 != null && !timerJob0.isActive && membersForEachJob0 != null && !membersForEachJob0.isActive) {
            launchMembersForEachJob()
        }
    }

    /**
     * Пропуск старта текущего участника.
     * Если он ещё не стартовал, двигаем его на одну позицию вниз. И запускаем таймер.
     * Если уже стартовал, то просто стартуем следующего участника.
     * Кнопку следующий можно нажаеть, только если статус = WAITING_START или STARTED_WAITING_FINISH.
     */
    fun onNext() {
        if (!isNextEnabled.value) return

        if (_status.value == Status.WAITING_START) {
            viewModelScope.launch {
                cancelJobs()
                val nextMemberIndex = event.currentMemberIndex + 1
                if (nextMemberIndex < members.size) {
                    Collections.swap(members, event.currentMemberIndex, nextMemberIndex)
                }
                launchMembersForEachJob()
            }
        } else if (_status.value == Status.STARTED_WAITING_FINISH) {
            viewModelScope.launch {
                cancelJobs()
                launchMembersForEachJob()
            }
        }
    }

    /**
     * Пропуск старта текущего участника. Двигаем его в конец списка. И запускаем таймер.
     * Кнопку следующий можно нажаеть, только если статус = WAITING_START.
     */
    fun toEnd() {
        if (!isToEndEnabled.value) return

        viewModelScope.launch {
            cancelJobs()
            val lastMemberIndex = members.size - 1
            Collections.swap(members, event.currentMemberIndex, lastMemberIndex)
            launchMembersForEachJob()
        }
    }

    private suspend fun cancelJobs() {
        membersForEachJob?.cancel()
        timerJob?.cancelAndJoin()
    }

    fun handleFinishActive(finishItem: ResultItem) {
        viewModelScope.launch {
            if (finishItem.isActive) return@launch
            resultRepository.makeFinishAsOnlyActiveOne(finishItem.id)
        }
    }

    private enum class Status {
        VIEW_ONLY,
        WAITING_START,
        STARTED_WAITING_FINISH,
        FINISHED,
        PAUSED
    }

    companion object {
        const val NULL_ID = 0L

        /** Время ожидания перед перед стартом участника. */
        private const val BEFORE_START_TIME = 5_000L

        private const val ONE_SECOND = 1000L
    }

}
