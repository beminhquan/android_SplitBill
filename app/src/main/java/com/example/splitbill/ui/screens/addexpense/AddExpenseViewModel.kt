package com.example.splitbill.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.local.entity.MemberEntity
import com.example.splitbill.data.repository.SplitBillRepository
import com.example.splitbill.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val members: List<MemberEntity> = emptyList(),
    val title: String = "",
    val amountInput: String = "",
    val parsedAmount: Double = 0.0,
    val selectedPayerId: Long? = null,
    val selectedParticipantIds: Set<Long> = emptySet(),
    val note: String = "",
    val amountPerPerson: Double = 0.0,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

class AddExpenseViewModel(
    private val groupId: Long,
    private val repository: SplitBillRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    private val _amountInput = MutableStateFlow("")
    private val _selectedPayerId = MutableStateFlow<Long?>(null)
    private val _selectedParticipantIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _note = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent

    private var initializedParticipants = false

    val uiState: StateFlow<AddExpenseUiState> = combine(
        repository.getMembersByGroupId(groupId),
        _title,
        _amountInput,
        _selectedPayerId,
        _selectedParticipantIds,
        _note,
        _errorMessage,
        _isSaving
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val members = args[0] as List<MemberEntity>
        val title = args[1] as String
        val amountInput = args[2] as String
        val selectedPayer = args[3] as Long?
        var participants = args[4] as Set<Long>
        val note = args[5] as String
        val error = args[6] as String?
        val isSaving = args[7] as Boolean

        // Default to all members and first member as payer on initial load
        var payerId = selectedPayer
        if (members.isNotEmpty() && !initializedParticipants) {
            initializedParticipants = true
            participants = members.map { it.id }.toSet()
            _selectedParticipantIds.value = participants
            if (payerId == null) {
                payerId = members.first().id
                _selectedPayerId.value = payerId
            }
        }

        val amount = CurrencyUtils.parseAmount(amountInput) ?: 0.0
        val perPerson = if (participants.isNotEmpty() && amount > 0) {
            amount / participants.size
        } else 0.0

        AddExpenseUiState(
            members = members,
            title = title,
            amountInput = amountInput,
            parsedAmount = amount,
            selectedPayerId = payerId,
            selectedParticipantIds = participants,
            note = note,
            amountPerPerson = perPerson,
            errorMessage = error,
            isSaving = isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        _errorMessage.value = null
    }

    fun onAmountChange(newAmount: String) {
        // Keep only digits
        val clean = newAmount.filter { it.isDigit() }
        _amountInput.value = clean
        _errorMessage.value = null
    }

    fun onPayerSelected(memberId: Long) {
        _selectedPayerId.value = memberId
        _errorMessage.value = null
    }

    fun toggleParticipant(memberId: Long) {
        val current = _selectedParticipantIds.value.toMutableSet()
        if (current.contains(memberId)) {
            if (current.size > 1) { // Keep at least one participant
                current.remove(memberId)
            }
        } else {
            current.add(memberId)
        }
        _selectedParticipantIds.value = current
    }

    fun selectAllParticipants(allMemberIds: List<Long>) {
        _selectedParticipantIds.value = allMemberIds.toSet()
    }

    fun onNoteChange(newNote: String) {
        _note.value = newNote
    }

    fun saveExpense() {
        val state = uiState.value
        val title = state.title.trim()
        val amount = state.parsedAmount
        val payerId = state.selectedPayerId
        val participants = state.selectedParticipantIds.toList()

        when {
            title.isBlank() -> {
                _errorMessage.value = "Vui lòng nhập tên khoản chi"
                return
            }
            amount <= 0 -> {
                _errorMessage.value = "Vui lòng nhập số tiền hợp lệ (> 0 ₫)"
                return
            }
            payerId == null -> {
                _errorMessage.value = "Vui lòng chọn người thanh toán"
                return
            }
            participants.isEmpty() -> {
                _errorMessage.value = "Vui lòng chọn ít nhất một người tham gia chia tiền"
                return
            }
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.addExpenseWithSplits(
                    groupId = groupId,
                    title = title,
                    amount = amount,
                    payerMemberId = payerId,
                    participantMemberIds = participants,
                    note = state.note
                )
                _saveSuccessEvent.emit(Unit)
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi lưu khoản chi: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    class Factory(
        private val groupId: Long,
        private val repository: SplitBillRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddExpenseViewModel(groupId, repository) as T
        }
    }
}
