package ru.axcheb.saigaktiming.ui.finish

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.domain.Finish
import ru.axcheb.saigaktiming.data.model.domain.Member
import ru.axcheb.saigaktiming.data.model.domain.Start
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository
import java.text.SimpleDateFormat
import java.util.*

class FinishViewModel(
    private val eventId: Long,
    private val memberId: Long,
    private val startId: Long,
    private val memberRepository: MemberRepository,
    private val resultRepository: ResultRepository,
    private val application: Application
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    /**
     * Flow событий срабатывания фотодатчика финиша.
     */
    private val _finishFlow = MutableSharedFlow<Date>(1, 0, BufferOverflow.DROP_OLDEST)

    private val _status = MutableStateFlow(Status.PAUSED)

    private val _millisFromStartCountingFlow = MutableStateFlow(0L)

    val timerStr
        get() = _millisFromStartCountingFlow.map { formatToTimeStr(it) }
            .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private fun formatToTimeStr(millisFromStartCounting: Long): String {
        return when (_status.value) {
            Status.WAITING_START -> {
                ((millisFromStartCounting / ONE_SECOND * ONE_SECOND - BEFORE_START_TIME) / ONE_SECOND).toString()
            }
            Status.STARTED_WAITING_FINISH -> {
                val waitingFinishSeconds =
                    (millisFromStartCounting - BEFORE_START_TIME) / ONE_SECOND
                if (waitingFinishSeconds == 0L) {
                    application.resources.getString(R.string.start_uppercase)
                } else {
                    DateUtils.formatElapsedTime(waitingFinishSeconds)
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

    /**
     * Список участников, которые выйдут на старт.
     */
    private var members: MutableList<Member> = arrayListOf()

    /**
     * Индекс текущего выбранного участника.
     */
    private var memberIndex = 0

    /**
     * Текущий выбранный участник.
     */
    private val _member: MutableStateFlow<Member?> = MutableStateFlow(null)
    val member: StateFlow<Member?> = _member

    private var timerJob: Job? = null
    private var membersForEachJob: Job? = null

    private val _startIdFlow = MutableStateFlow(startId)
    private val start: SharedFlow<Start?> =
        _startIdFlow.flatMapLatest { startId -> resultRepository.getStart(startId) }
            .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    val startTimeStr =
        start.map { start -> if (start == null) "" else dateFormat.format(start.time) }
            .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val finishItems: StateFlow<List<ResultItem>> =
        _startIdFlow.flatMapLatest { startId -> resultRepository.getFinishResults(startId) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isNextEnabled: StateFlow<Boolean> =
        _status.map { it == Status.WAITING_START || it == Status.STARTED_WAITING_FINISH }
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

    init {
        viewModelScope.launch {
            if (startId != NULL_ID) {  // Если startId задан, вьюха открывается только на просмотр.
                _status.value = Status.VIEW_ONLY
                _startIdFlow.value = startId
            }

            if (memberId != NULL_ID) {
                val m = memberRepository.getMember(memberId).firstOrNull()
                m?.apply { members = arrayListOf(this) }
            } else {
                val memberList = memberRepository.getMembers(eventId).firstOrNull()
                memberList?.apply { members = this.toMutableList() }
            }

            if (_status.value == Status.VIEW_ONLY) {
                if (members.isNotEmpty()) {
                    _member.value = members[0]
                }
            } else {
                memberIndex = 0
                // Запускаю обработку всех member, что удалось загрузить в memberList
                launchMembersForEachJob()
            }
        }

        _finishFlow.onEach {
            // Если статус STARTED_WAITING_FINISH, то при каждом срабатывании датчика финиша, сохраняется финишная отметка.
            val startId = _startIdFlow.value
            if (startId != NULL_ID && _status.value == Status.STARTED_WAITING_FINISH) {
                val finish = Finish(null, startId, it, 1, true)
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
    }

    fun newFinish() {
        val date = Date()
        _finishFlow.tryEmit(date)
    }

    private fun launchMembersForEachJob(): Job {
        val job = viewModelScope.launch {
            while (memberIndex < members.size) {
                _member.value = members[memberIndex]
                _startIdFlow.value = 0
                val job = launchTimerJob()
                job.join()
                delay(BETWEEN_MEMBERS_TIME)
                memberIndex++
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

        val startCountingMillis = System.currentTimeMillis()
        val endMillis = BEFORE_START_TIME + WAITING_FINISH_TIME
        val job = viewModelScope.launch {
            var current = System.currentTimeMillis()
            var fromStartCounting = current - startCountingMillis
            try {
                onTimerStarted()
                while (fromStartCounting < endMillis) {
                    if (fromStartCounting < BEFORE_START_TIME) {
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
        } else if (_status.value != Status.STARTED_WAITING_FINISH) {
            throw IllegalStateException("Illegal state ${_status.value}")
        }
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    private fun onTimerCanceled(millisFromStartCounting: Long) {
        _status.value =
            Status.PAUSED // TODO возможно ввести другой статус или просто блокировать кнопку Следующий, когда остался один участник.
        _millisFromStartCountingFlow.value = millisFromStartCounting
    }

    private fun onTimerFinished(millisFromStartCounting: Long) {
        if (_status.value == Status.STARTED_WAITING_FINISH) {
            _status.value = Status.FINISHED
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
            if (_status.value == Status.WAITING_START) {
                cancelJobs()
            } else if (_status.value == Status.STARTED_WAITING_FINISH || _status.value == Status.FINISHED) {
                cancelJobs()
                memberIndex++
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
     * Пропуск старта текущего участника. Двигаем его на одну позицию вниз. И запускаем таймер.
     * Кнопку следующий можно нажаеть, только если статус = WAITING_START.
     */
    fun onNext() {
        if (!isNextEnabled.value) return

        if (_status.value == Status.WAITING_START) {
            viewModelScope.launch {
                cancelJobs()
                val nextMemberIndex = memberIndex + 1
                if (nextMemberIndex < members.size) {
                    Collections.swap(members, memberIndex, nextMemberIndex)
                }
                launchMembersForEachJob()
            }
        } else if (_status.value == Status.STARTED_WAITING_FINISH) {
            viewModelScope.launch {
                cancelJobs()
                memberIndex++
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
            Collections.swap(members, memberIndex, lastMemberIndex)
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
        private const val BEFORE_START_TIME = 10_000L

        /** Время отведённое на прохождение дистанции. */
        private const val WAITING_FINISH_TIME = 10_000L

        /** Время между окончанием финишного времени и началом времени ожидания следующего участника. */
        private const val BETWEEN_MEMBERS_TIME = 3_000L
        private const val ONE_SECOND = 1000L

        private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    }

}
