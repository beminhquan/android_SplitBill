package com.example.splitbill.ui.screens.groupdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.local.entity.GroupEntity
import com.example.splitbill.data.local.entity.MemberEntity
import com.example.splitbill.data.model.ExpenseDetail
import com.example.splitbill.data.repository.SplitBillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: GroupEntity? = null,
    val members: List<MemberEntity> = emptyList(),
    val expenses: List<ExpenseDetail> = emptyList(),
    val totalExpense: Double = 0.0,
    val selectedTab: Int = 0, // 0: Expenses, 1: Members
    val showAddMemberDialog: Boolean = false,
    val isLoading: Boolean = false
)

class GroupDetailViewModel(
    private val groupId: Long,
    private val repository: SplitBillRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _showAddMemberDialog = MutableStateFlow(false)

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.getGroupById(groupId),
        repository.getMembersByGroupId(groupId),
        repository.getExpensesByGroupId(groupId),
        _selectedTab,
        _showAddMemberDialog
    ) { group, members, expenses, tab, showDialog ->
        val total = expenses.sumOf { it.expense.amount }
        GroupDetailUiState(
            group = group,
            members = members,
            expenses = expenses,
            totalExpense = total,
            selectedTab = tab,
            showAddMemberDialog = showDialog,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupDetailUiState(isLoading = true)
    )

    fun onSelectTab(index: Int) {
        _selectedTab.value = index
    }

    fun onOpenAddMemberDialog() {
        _showAddMemberDialog.value = true
    }

    fun onDismissAddMemberDialog() {
        _showAddMemberDialog.value = false
    }

    fun addMember(name: String, phone: String, colorIndex: Int) {
        viewModelScope.launch {
            repository.addMember(groupId, name, phone, colorIndex)
            _showAddMemberDialog.value = false
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    class Factory(
        private val groupId: Long,
        private val repository: SplitBillRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroupDetailViewModel(groupId, repository) as T
        }
    }
}
