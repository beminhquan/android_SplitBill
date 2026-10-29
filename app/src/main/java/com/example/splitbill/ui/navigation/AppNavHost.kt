package com.example.splitbill.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitbill.SplitBillApplication
import com.example.splitbill.ui.screens.addexpense.AddExpenseScreen
import com.example.splitbill.ui.screens.addexpense.AddExpenseViewModel
import com.example.splitbill.ui.screens.groupdetail.GroupDetailScreen
import com.example.splitbill.ui.screens.groupdetail.GroupDetailViewModel
import com.example.splitbill.ui.screens.grouplist.GroupListScreen
import com.example.splitbill.ui.screens.grouplist.GroupListViewModel
import com.example.splitbill.ui.screens.settlement.SettlementScreen
import com.example.splitbill.ui.screens.settlement.SettlementViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as SplitBillApplication
    val repository = app.repository

    NavHost(
        navController = navController,
        startDestination = Screen.GroupList.route,
        modifier = modifier
    ) {
        // Screen 1: Group List & Create
        composable(Screen.GroupList.route) {
            val viewModel: GroupListViewModel = viewModel(
                factory = GroupListViewModel.Factory(repository)
            )
            GroupListScreen(
                viewModel = viewModel,
                onGroupClick = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }

        // Screen 2: Group Detail (Expenses & Members)
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: return@composable
            val viewModel: GroupDetailViewModel = viewModel(
                factory = GroupDetailViewModel.Factory(groupId, repository)
            )
            GroupDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
                },
                onNavigateToSettlement = {
                    navController.navigate(Screen.Settlement.createRoute(groupId))
                }
            )
        }

        // Screen 3: Add Expense
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: return@composable
            val viewModel: AddExpenseViewModel = viewModel(
                factory = AddExpenseViewModel.Factory(groupId, repository)
            )
            AddExpenseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Screen 4 & 5: Settlement & Debt Reminders
        composable(
            route = Screen.Settlement.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: return@composable
            val viewModel: SettlementViewModel = viewModel(
                factory = SettlementViewModel.Factory(groupId, repository)
            )
            SettlementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
