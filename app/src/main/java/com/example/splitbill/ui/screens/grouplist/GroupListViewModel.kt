package com.example.splitbill.ui.screens.grouplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.model.GroupSummary
import com.example.splitbill.data.repository.SplitBillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupListUiState(
    val groups: List<GroupSummary> = emptyList(),
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false
)

class GroupListViewModel(
    private val repository: SplitBillRepository
) : ViewModel() {

    private val _showCreateDialog = MutableStateFlow(false)

    val uiState: StateFlow<GroupListUiState> = combine(
        repository.getAllGroupSummaries(),
        _showCreateDialog
    ) { groups, showDialog ->
        GroupListUiState(
            groups = groups,
            isLoading = false,
            showCreateDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupListUiState(isLoading = true)
    )

    fun onOpenCreateDialog() {
        _showCreateDialog.value = true
    }

    fun onDismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun createGroup(name: String, description: String, colorIndex: Int) {
        viewModelScope.launch {
            repository.createGroup(name, description, colorIndex)
            _showCreateDialog.value = false
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
        }
    }

    class Factory(private val repository: SplitBillRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroupListViewModel(repository) as T
        }
    }
}
