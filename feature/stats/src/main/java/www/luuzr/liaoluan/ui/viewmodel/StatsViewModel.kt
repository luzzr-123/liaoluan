package www.luuzr.liaoluan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.data.repository.PlannerRepository
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class StatsViewState(
    val viewingYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val viewingMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val logsForMonth: List<HabitLogEntity> = emptyList(),
    val habits: List<Habit> = emptyList() // Synced from repository
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: PlannerRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(StatsViewState())
    val viewState: StateFlow<StatsViewState> = _viewState.asStateFlow()

    init {
        // Source habits
        viewModelScope.launch {
            repository.habits.collect { h ->
                _viewState.update { it.copy(habits = h) }
            }
        }
        
        // Listen to Month/Year changes and fetch appropriate data range
        viewModelScope.launch {
            _viewState.map { Pair(it.viewingYear, it.viewingMonth) }
                .distinctUntilChanged()
                .collectLatest { (year, month) ->
                    loadLogsForMonth(year, month)
                }
        }
    }

    private suspend fun loadLogsForMonth(year: Int, month: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        // Adjust to grab the 42 days grid span (might include previous/next month days)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = firstDayOfWeek - Calendar.SUNDAY
        cal.add(Calendar.DAY_OF_MONTH, -offset)
        
        // BUG-6 Fix: 与 DateHandle 保持一致使用 Locale.US
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val startDateStr = sdf.format(cal.time)
        
        cal.add(Calendar.DAY_OF_MONTH, 41) // 42 days window
        val endDateStr = sdf.format(cal.time)
        
        // Collect logs for this precise 42-day window from DB
        repository.getHabitLogsBetween(startDateStr, endDateStr).collect { logs ->
            _viewState.update { it.copy(logsForMonth = logs) }
        }
    }
    
    fun setViewingMonth(year: Int, month: Int) {
        _viewState.update { it.copy(viewingYear = year, viewingMonth = month) }
    }
    
    fun previousMonth() {
        _viewState.update { 
            if (it.viewingMonth == 1) {
                it.copy(viewingMonth = 12, viewingYear = it.viewingYear - 1)
            } else {
                it.copy(viewingMonth = it.viewingMonth - 1)
            }
        }
    }
    
    fun nextMonth() {
        _viewState.update { 
            if (it.viewingMonth == 12) {
                it.copy(viewingMonth = 1, viewingYear = it.viewingYear + 1)
            } else {
                it.copy(viewingMonth = it.viewingMonth + 1)
            }
        }
    }
}
