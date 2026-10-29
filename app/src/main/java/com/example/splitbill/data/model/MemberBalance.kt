package com.example.splitbill.data.model

data class MemberBalance(
    val memberId: Long,
    val memberName: String,
    val phoneNumber: String = "",
    val avatarColorIndex: Int = 0,
    val totalPaid: Double = 0.0,
    val totalOwed: Double = 0.0,
    val netBalance: Double = 0.0 // totalPaid - totalOwed
)
