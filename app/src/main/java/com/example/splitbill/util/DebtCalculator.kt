package com.example.splitbill.util

import com.example.splitbill.data.local.entity.ExpenseEntity
import com.example.splitbill.data.local.entity.ExpenseSplitEntity
import com.example.splitbill.data.local.entity.MemberEntity
import com.example.splitbill.data.model.DebtSettlement
import com.example.splitbill.data.model.MemberBalance
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToLong

object DebtCalculator {

    private const val EPSILON = 0.01

    /**
     * Calculates net balance for every member in the group.
     * Net Balance = Total Amount Paid by Member - Total Amount Owed by Member.
     * Positive (> 0): Creditor (member is owed money)
     * Negative (< 0): Debtor (member owes money)
     */
    fun calculateMemberBalances(
        members: List<MemberEntity>,
        expenses: List<ExpenseEntity>,
        splits: List<ExpenseSplitEntity>
    ): List<MemberBalance> {
        val memberMap = members.associateBy { it.id }

        // Total paid by member
        val paidMap = mutableMapOf<Long, Double>()
        for (expense in expenses) {
            paidMap[expense.payerMemberId] = (paidMap[expense.payerMemberId] ?: 0.0) + expense.amount
        }

        // Total owed by member
        val owedMap = mutableMapOf<Long, Double>()
        for (split in splits) {
            owedMap[split.memberId] = (owedMap[split.memberId] ?: 0.0) + split.splitAmount
        }

        return members.map { member ->
            val totalPaid = paidMap[member.id] ?: 0.0
            val totalOwed = owedMap[member.id] ?: 0.0
            val rawNet = totalPaid - totalOwed
            val cleanNet = if (abs(rawNet) < EPSILON) 0.0 else roundToTwoDecimals(rawNet)

            MemberBalance(
                memberId = member.id,
                memberName = member.name,
                phoneNumber = member.phoneNumber,
                avatarColorIndex = member.avatarColorIndex,
                totalPaid = roundToTwoDecimals(totalPaid),
                totalOwed = roundToTwoDecimals(totalOwed),
                netBalance = cleanNet
            )
        }
    }

    /**
     * Calculates optimal debt settlement transactions using greedy minimum cash flow algorithm.
     * Minimizes the number of transactions required to settle all debts.
     */
    fun calculateDebtSettlement(
        balances: List<MemberBalance>,
        groupName: String = ""
    ): List<DebtSettlement> {
        data class SettlementParty(
            val memberId: Long,
            val name: String,
            val phone: String,
            var amount: Double
        )

        val debtors = mutableListOf<SettlementParty>()
        val creditors = mutableListOf<SettlementParty>()

        for (balance in balances) {
            if (balance.netBalance < -EPSILON) {
                debtors.add(
                    SettlementParty(
                        memberId = balance.memberId,
                        name = balance.memberName,
                        phone = balance.phoneNumber,
                        amount = abs(balance.netBalance)
                    )
                )
            } else if (balance.netBalance > EPSILON) {
                creditors.add(
                    SettlementParty(
                        memberId = balance.memberId,
                        name = balance.memberName,
                        phone = balance.phoneNumber,
                        amount = balance.netBalance
                    )
                )
            }
        }

        debtors.sortByDescending { it.amount }
        creditors.sortByDescending { it.amount }

        val settlements = mutableListOf<DebtSettlement>()
        var debtorIdx = 0
        var creditorIdx = 0

        while (debtorIdx < debtors.size && creditorIdx < creditors.size) {
            val debtor = debtors[debtorIdx]
            val creditor = creditors[creditorIdx]

            val settleAmount = min(debtor.amount, creditor.amount)
            if (settleAmount > EPSILON) {
                settlements.add(
                    DebtSettlement(
                        fromMemberId = debtor.memberId,
                        fromMemberName = debtor.name,
                        fromMemberPhone = debtor.phone,
                        toMemberId = creditor.memberId,
                        toMemberName = creditor.name,
                        toMemberPhone = creditor.phone,
                        amount = roundToTwoDecimals(settleAmount),
                        groupName = groupName
                    )
                )
            }

            debtor.amount -= settleAmount
            creditor.amount -= settleAmount

            if (debtor.amount < EPSILON) {
                debtorIdx++
            }
            if (creditor.amount < EPSILON) {
                creditorIdx++
            }
        }

        return settlements
    }

    /**
     * Splits an expense evenly among participant members.
     * Distributes any rounding residue to ensure sum(splits) == totalAmount.
     */
    fun splitEqually(
        totalAmount: Double,
        participantMemberIds: List<Long>
    ): List<ExpenseSplitEntity> {
        if (participantMemberIds.isEmpty() || totalAmount <= 0.0) return emptyList()

        val count = participantMemberIds.size
        // Calculate base split rounded to 2 decimals
        val rawBase = totalAmount / count
        val baseSplit = roundToTwoDecimals(rawBase)
        val splits = mutableListOf<ExpenseSplitEntity>()

        var accumulated = 0.0
        participantMemberIds.forEachIndexed { index, memberId ->
            val splitAmount = if (index == count - 1) {
                roundToTwoDecimals(totalAmount - accumulated)
            } else {
                baseSplit
            }
            accumulated += splitAmount
            splits.add(
                ExpenseSplitEntity(
                    expenseId = 0, // Assigned upon insert
                    memberId = memberId,
                    splitAmount = splitAmount
                )
            )
        }

        return splits
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToLong() / 100.0
    }
}
