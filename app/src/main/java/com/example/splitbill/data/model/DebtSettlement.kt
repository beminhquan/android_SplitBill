package com.example.splitbill.data.model

data class DebtSettlement(
    val fromMemberId: Long,
    val fromMemberName: String,
    val fromMemberPhone: String = "",
    val toMemberId: Long,
    val toMemberName: String,
    val toMemberPhone: String = "",
    val amount: Double,
    val groupName: String = ""
)
