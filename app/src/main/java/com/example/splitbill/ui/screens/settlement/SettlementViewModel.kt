package com.example.splitbill.ui.screens.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.model.DebtSettlement
import com.example.splitbill.data.model.MemberBalance
import com.example.splitbill.data.repository.SplitBillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SettlementUiState(
    val groupName: String = "",
    val balances: List<MemberBalance> = emptyList(),
    val settlements: List<DebtSettlement> = emptyList(),
    val selectedDebtForReminder: DebtSettlement? = null,
    val isLoading: Boolean = false
)

class SettlementViewModel(
    private val groupId: Long,
    private val repository: SplitBillRepository
) : ViewModel() {

    private val _selectedDebtForReminder = MutableStateFlow<DebtSettlement?>(null)

    val uiState: StateFlow<SettlementUiState> = combine(
        repository.getGroupById(groupId),
        repository.getMemberBalances(groupId),
        repository.getDebtSettlements(groupId),
        _selectedDebtForReminder
    ) { group, balances, settlements, selectedDebt ->
        SettlementUiState(
            groupName = group?.name ?: "",
            balances = balances,
            settlements = settlements,
            selectedDebtForReminder = selectedDebt,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettlementUiState(isLoading = true)
    )

    fun onSelectDebtForReminder(debt: DebtSettlement) {
        _selectedDebtForReminder.value = debt
    }

    fun onDismissReminder() {
        _selectedDebtForReminder.value = null
    }

    class Factory(
        private val groupId: Long,
        private val repository: SplitBillRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettlementViewModel(groupId, repository) as T
        }
    }
}
