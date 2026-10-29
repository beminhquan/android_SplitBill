package com.example.splitbill.data.model

import androidx.room.Embedded
import com.example.splitbill.data.local.entity.GroupEntity

data class GroupSummary(
    @Embedded
    val group: GroupEntity,
    val memberCount: Int = 0,
    val totalExpense: Double = 0.0
)
