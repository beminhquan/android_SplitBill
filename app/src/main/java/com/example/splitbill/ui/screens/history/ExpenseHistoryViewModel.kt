package com.example.splitbill.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.model.ExpenseDetail
import com.example.splitbill.data.local.entity.GroupEntity
import com.example.splitbill.data.repository.SplitBillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExpenseHistoryViewModel(
    private val repository: SplitBillRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId.asStateFlow()

    val groups: StateFlow<List<GroupEntity>> = repository.getAllGroupSummaries()
        .combine(MutableStateFlow(emptyList<GroupEntity>())) { summaries, _ ->
            summaries.map { it.group }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<ExpenseDetail>> = combine(
        repository.getAllExpenses(),
        _searchQuery,
        _selectedGroupId
    ) { expenses, query, groupId ->
        expenses.filter { detail ->
            val matchesQuery = detail.expense.title.contains(query, ignoreCase = true) ||
                    detail.expense.note.contains(query, ignoreCase = true)
            val matchesGroup = groupId == null || detail.expense.groupId == groupId
            matchesQuery && matchesGroup
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGroupSelected(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    class Factory(private val repository: SplitBillRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseHistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseHistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
