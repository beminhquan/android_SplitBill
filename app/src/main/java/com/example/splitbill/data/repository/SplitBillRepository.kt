package com.example.splitbill.data.repository

import com.example.splitbill.data.local.entity.ExpenseEntity
import com.example.splitbill.data.local.entity.ExpenseSplitEntity
import com.example.splitbill.data.local.entity.GroupEntity
import com.example.splitbill.data.local.entity.MemberEntity
import com.example.splitbill.data.model.DebtSettlement
import com.example.splitbill.data.model.ExpenseDetail
import com.example.splitbill.data.model.GroupSummary
import com.example.splitbill.data.model.MemberBalance
import kotlinx.coroutines.flow.Flow

interface SplitBillRepository {
    // Group operations
    fun getAllGroupSummaries(): Flow<List<GroupSummary>>
    fun getGroupById(groupId: Long): Flow<GroupEntity?>
    suspend fun createGroup(name: String, description: String, colorIndex: Int = 0): Long
    suspend fun deleteGroup(groupId: Long)

    // Member operations
    fun getMembersByGroupId(groupId: Long): Flow<List<MemberEntity>>
    suspend fun addMember(groupId: Long, name: String, phoneNumber: String = "", avatarColorIndex: Int = 0): Long
    suspend fun deleteMember(member: MemberEntity)

    // Expense operations
    fun getExpensesByGroupId(groupId: Long): Flow<List<ExpenseDetail>>
    fun getAllExpenses(): Flow<List<ExpenseDetail>>
    suspend fun addExpenseWithSplits(
        groupId: Long,
        title: String,
        amount: Double,
        payerMemberId: Long,
        participantMemberIds: List<Long>,
        note: String = ""
    ): Long
    suspend fun deleteExpense(expenseId: Long)

    // Statistics & Settlement
    fun getMemberBalances(groupId: Long): Flow<List<MemberBalance>>
    fun getDebtSettlements(groupId: Long): Flow<List<DebtSettlement>>
}
