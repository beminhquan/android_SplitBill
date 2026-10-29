package com.example.splitbill.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object GroupList : Screen("group_list", "Nhóm", Icons.Default.Groups)
    object ExpenseHistory : Screen("expense_history", "Lịch sử", Icons.Default.History)
    object StatsSettings : Screen("stats_settings", "Cài đặt", Icons.Default.Settings)

    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: Long) = "add_expense/$groupId"
    }
    object Settlement : Screen("settlement/{groupId}") {
        fun createRoute(groupId: Long) = "settlement/$groupId"
    }
}
