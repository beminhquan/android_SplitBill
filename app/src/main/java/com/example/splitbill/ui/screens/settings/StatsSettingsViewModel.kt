package com.example.splitbill.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.model.GroupSummary
import com.example.splitbill.data.repository.SplitBillRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OverallStats(
    val totalExpense: Double = 0.0,
    val totalGroups: Int = 0,
    val groupSummaries: List<GroupSummary> = emptyList()
)

class StatsSettingsViewModel(
    private val repository: SplitBillRepository
) : ViewModel() {

    val stats: StateFlow<OverallStats> = repository.getAllGroupSummaries()
        .map { summaries ->
            OverallStats(
                totalExpense = summaries.sumOf { it.totalExpense },
                totalGroups = summaries.size,
                groupSummaries = summaries
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())

    class Factory(private val repository: SplitBillRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatsSettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
