package com.example.splitbill.data.model

import com.example.splitbill.data.local.entity.ExpenseEntity
import com.example.splitbill.data.local.entity.ExpenseSplitEntity

data class ExpenseDetail(
    val expense: ExpenseEntity,
    val payerName: String,
    val splits: List<ExpenseSplitEntity> = emptyList()
)
