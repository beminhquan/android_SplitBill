package com.example.splitbill.data.repository

import com.example.splitbill.data.local.dao.ExpenseDao
import com.example.splitbill.data.local.dao.GroupDao
import com.example.splitbill.data.local.dao.MemberDao
import com.example.splitbill.data.local.entity.ExpenseEntity
import com.example.splitbill.data.local.entity.GroupEntity
import com.example.splitbill.data.local.entity.MemberEntity
import com.example.splitbill.data.model.DebtSettlement
import com.example.splitbill.data.model.ExpenseDetail
import com.example.splitbill.data.model.GroupSummary
import com.example.splitbill.data.model.MemberBalance
import com.example.splitbill.util.DebtCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SplitBillRepositoryImpl(
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val expenseDao: ExpenseDao
) : SplitBillRepository {

    override fun getAllGroupSummaries(): Flow<List<GroupSummary>> {
        return groupDao.getAllGroupSummaries()
    }

    override fun getGroupById(groupId: Long): Flow<GroupEntity?> {
        return groupDao.getGroupById(groupId)
    }

    override suspend fun createGroup(name: String, description: String, colorIndex: Int): Long {
        val group = GroupEntity(
            name = name.trim(),
            description = description.trim(),
            colorIndex = colorIndex
        )
        return groupDao.insertGroup(group)
    }

    override suspend fun deleteGroup(groupId: Long) {
        groupDao.deleteGroupById(groupId)
    }

    override fun getMembersByGroupId(groupId: Long): Flow<List<MemberEntity>> {
        return memberDao.getMembersByGroupId(groupId)
    }

    override suspend fun addMember(
        groupId: Long,
        name: String,
        phoneNumber: String,
        avatarColorIndex: Int
    ): Long {
        val member = MemberEntity(
            groupId = groupId,
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            avatarColorIndex = avatarColorIndex
        )
        return memberDao.insertMember(member)
    }

    override suspend fun deleteMember(member: MemberEntity) {
        memberDao.deleteMember(member)
    }

    override fun getExpensesByGroupId(groupId: Long): Flow<List<ExpenseDetail>> {
        return combine(
            expenseDao.getExpensesByGroupId(groupId),
            memberDao.getMembersByGroupId(groupId),
            expenseDao.getSplitsByGroupIdFlow(groupId)
        ) { expenses, members, splits ->
            val memberMap = members.associateBy { it.id }
            val splitMap = splits.groupBy { it.expenseId }
            expenses.map { expense ->
                val payerName = memberMap[expense.payerMemberId]?.name ?: "Thành viên đã xóa"
                ExpenseDetail(
                    expense = expense,
                    payerName = payerName,
                    splits = splitMap[expense.id] ?: emptyList()
                )
            }
        }
    }

    override suspend fun addExpenseWithSplits(
        groupId: Long,
        title: String,
        amount: Double,
        payerMemberId: Long,
        participantMemberIds: List<Long>,
        note: String
    ): Long {
        val expense = ExpenseEntity(
            groupId = groupId,
            title = title.trim(),
            amount = amount,
            payerMemberId = payerMemberId,
            note = note.trim()
        )
        val splits = DebtCalculator.splitEqually(amount, participantMemberIds)
        return expenseDao.insertExpenseWithSplits(expense, splits)
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }

    override fun getMemberBalances(groupId: Long): Flow<List<MemberBalance>> {
        return combine(
            memberDao.getMembersByGroupId(groupId),
            expenseDao.getExpensesByGroupId(groupId),
            expenseDao.getSplitsByGroupIdFlow(groupId)
        ) { members, expenses, splits ->
            DebtCalculator.calculateMemberBalances(members, expenses, splits)
        }
    }

    override fun getDebtSettlements(groupId: Long): Flow<List<DebtSettlement>> {
        return combine(
            getMemberBalances(groupId),
            groupDao.getGroupById(groupId)
        ) { balances, group ->
            val groupName = group?.name ?: ""
            DebtCalculator.calculateDebtSettlement(balances, groupName)
        }
    }
}
