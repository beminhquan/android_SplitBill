package com.example.splitbill.ui.navigation

sealed class Screen(val route: String) {
    object GroupList : Screen("group_list")
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
