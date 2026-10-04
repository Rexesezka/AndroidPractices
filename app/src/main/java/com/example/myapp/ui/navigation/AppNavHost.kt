package com.example.myapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.ui.common.PlaceholderScreen
import com.example.myapp.ui.games.GameDetailsScreen
import com.example.myapp.ui.games.GameListScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Destinations.GAMES, "Матчи", Icons.Filled.Home),
    BottomNavItem(Destinations.TEAMS, "Команды", Icons.Filled.AccountCircle),
    BottomNavItem(Destinations.PLAYERS, "Игроки", Icons.Filled.Person),
    BottomNavItem(Destinations.LEAGUES, "Лиги", Icons.AutoMirrored.Filled.List),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.GAMES,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destinations.GAMES) {
                GameListScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Destinations.gameDetails(gameId))
                    },
                )
            }
            composable(
                route = Destinations.GAME_DETAILS,
                arguments = listOf(
                    navArgument(Destinations.ARG_GAME_ID) { type = NavType.IntType },
                ),
            ) { entry ->
                val gameId = entry.arguments?.getInt(Destinations.ARG_GAME_ID) ?: 0
                GameDetailsScreen(
                    gameId = gameId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destinations.TEAMS) {
                PlaceholderScreen("Команды", "Список команд появится в следующих практиках")
            }
            composable(Destinations.PLAYERS) {
                PlaceholderScreen("Игроки", "Список игроков появится в следующих практиках")
            }
            composable(Destinations.LEAGUES) {
                PlaceholderScreen("Лиги", "Список лиг появится в следующих практиках")
            }
        }
    }
}
