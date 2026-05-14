package com.example.beerzaao.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beerzaao.ui.addfund.AddFundScreen
import com.example.beerzaao.ui.detail.DetailScreen
import com.example.beerzaao.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_FUND = "add_fund"
    const val DETAIL = "detail/{fundCode}"

    fun detail(fundCode: String) = "detail/$fundCode"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAddFund = { navController.navigate(Routes.ADD_FUND) },
                onNavigateToDetail = { fundCode ->
                    navController.navigate(Routes.detail(fundCode))
                }
            )
        }

        composable(Routes.ADD_FUND) {
            AddFundScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("fundCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val fundCode = backStackEntry.arguments?.getString("fundCode") ?: ""
            DetailScreen(
                fundCode = fundCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
