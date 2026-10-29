package com.example.splitbill.util

import com.example.splitbill.data.local.entity.ExpenseEntity
import com.example.splitbill.data.local.entity.ExpenseSplitEntity
import com.example.splitbill.data.local.entity.MemberEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DebtCalculatorTest {

    @Test
    fun testSingleExpenseSplitEqually() {
        // Members: An (id 1), Binh (id 2), Chau (id 3)
        val members = listOf(
            MemberEntity(id = 1, groupId = 1, name = "An"),
            MemberEntity(id = 2, groupId = 1, name = "Bình"),
            MemberEntity(id = 3, groupId = 1, name = "Châu")
        )

        // Expense: An paid 300,000 VND for all 3 members
        val expenses = listOf(
            ExpenseEntity(id = 1, groupId = 1, title = "Lẩu gà", amount = 300000.0, payerMemberId = 1)
        )
        val splits = listOf(
            ExpenseSplitEntity(id = 1, expenseId = 1, memberId = 1, splitAmount = 100000.0),
            ExpenseSplitEntity(id = 2, expenseId = 1, memberId = 2, splitAmount = 100000.0),
            ExpenseSplitEntity(id = 3, expenseId = 1, memberId = 3, splitAmount = 100000.0)
        )

        val balances = DebtCalculator.calculateMemberBalances(members, expenses, splits)

        val anBalance = balances.find { it.memberId == 1L }!!
        val binhBalance = balances.find { it.memberId == 2L }!!
        val chauBalance = balances.find { it.memberId == 3L }!!

        // An: paid 300k, share 100k -> net +200k
        assertEquals(200000.0, anBalance.netBalance, 0.01)
        // Binh: paid 0k, share 100k -> net -100k
        assertEquals(-100000.0, binhBalance.netBalance, 0.01)
        // Chau: paid 0k, share 100k -> net -100k
        assertEquals(-100000.0, chauBalance.netBalance, 0.01)

        // Optimal settlements
        val settlements = DebtCalculator.calculateDebtSettlement(balances, "Du lịch")
        assertEquals(2, settlements.size)

        // Both Binh and Chau should pay An 100,000 each
        assertTrue(settlements.all { it.toMemberId == 1L && it.amount == 100000.0 })
    }

    @Test
    fun testMultipleOverlappingExpenses() {
        val members = listOf(
            MemberEntity(id = 1, groupId = 1, name = "An"),
            MemberEntity(id = 2, groupId = 1, name = "Bình"),
            MemberEntity(id = 3, groupId = 1, name = "Châu")
        )

        // Expense 1: An paid 300,000 VND (100k each)
        // Expense 2: Binh paid 150,000 VND (50k each)
        val expenses = listOf(
            ExpenseEntity(id = 1, groupId = 1, title = "Lẩu", amount = 300000.0, payerMemberId = 1),
            ExpenseEntity(id = 2, groupId = 1, title = "Trà sữa", amount = 150000.0, payerMemberId = 2)
        )
        val splits = listOf(
            ExpenseSplitEntity(id = 1, expenseId = 1, memberId = 1, splitAmount = 100000.0),
            ExpenseSplitEntity(id = 2, expenseId = 1, memberId = 2, splitAmount = 100000.0),
            ExpenseSplitEntity(id = 3, expenseId = 1, memberId = 3, splitAmount = 100000.0),
            ExpenseSplitEntity(id = 4, expenseId = 2, memberId = 1, splitAmount = 50000.0),
            ExpenseSplitEntity(id = 5, expenseId = 2, memberId = 2, splitAmount = 50000.0),
            ExpenseSplitEntity(id = 6, expenseId = 2, memberId = 3, splitAmount = 50000.0)
        )

        val balances = DebtCalculator.calculateMemberBalances(members, expenses, splits)

        val anBalance = balances.find { it.memberId == 1L }!!
        val binhBalance = balances.find { it.memberId == 2L }!!
        val chauBalance = balances.find { it.memberId == 3L }!!

        // An: paid 300k, share 150k -> +150k
        assertEquals(150000.0, anBalance.netBalance, 0.01)
        // Binh: paid 150k, share 150k -> 0k
        assertEquals(0.0, binhBalance.netBalance, 0.01)
        // Chau: paid 0k, share 150k -> -150k
        assertEquals(-150000.0, chauBalance.netBalance, 0.01)

        val settlements = DebtCalculator.calculateDebtSettlement(balances, "Du lịch")
        // Only 1 settlement: Chau pays An 150k directly!
        assertEquals(1, settlements.size)
        assertEquals("Châu", settlements[0].fromMemberName)
        assertEquals("An", settlements[0].toMemberName)
        assertEquals(150000.0, settlements[0].amount, 0.01)
    }

    @Test
    fun testSplitEquallyRoundingPreservation() {
        val participants = listOf(1L, 2L, 3L)
        val totalAmount = 100000.0

        val splits = DebtCalculator.splitEqually(totalAmount, participants)
        assertEquals(3, splits.size)

        val sumSplits = splits.sumOf { it.splitAmount }
        // Exact sum must equal totalAmount
        assertEquals(totalAmount, sumSplits, 0.001)
    }

    @Test
    fun testZeroSumInvariant() {
        val members = listOf(
            MemberEntity(id = 1, groupId = 1, name = "A"),
            MemberEntity(id = 2, groupId = 1, name = "B"),
            MemberEntity(id = 3, groupId = 1, name = "C"),
            MemberEntity(id = 4, groupId = 1, name = "D")
        )

        val expenses = listOf(
            ExpenseEntity(id = 1, groupId = 1, title = "Xe", amount = 450000.0, payerMemberId = 1),
            ExpenseEntity(id = 2, groupId = 1, title = "Ăn", amount = 620000.0, payerMemberId = 3)
        )
        val splits = DebtCalculator.splitEqually(450000.0, listOf(1L, 2L, 3L, 4L)) +
                DebtCalculator.splitEqually(620000.0, listOf(1L, 2L, 3L, 4L))

        val balances = DebtCalculator.calculateMemberBalances(members, expenses, splits)
        val sumNetBalances = balances.sumOf { it.netBalance }

        // Sum of all net balances must equal 0
        assertTrue("Sum of net balances should be approximately 0", abs(sumNetBalances) < 0.01)
    }
}
