package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.F1ViewModel
import com.example.ui.F1ViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel using Factory
        val viewModel = ViewModelProvider(
            this, 
            F1ViewModelFactory(application)
        )[F1ViewModel::class.java]

        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Primary tabs definitions
                val items = listOf(
                    NavigationItem("dashboard", "Home", Icons.Default.Home, Icons.Outlined.Home),
                    NavigationItem("racing", "Racing", Icons.Default.SportsScore, Icons.Outlined.SportsScore),
                    NavigationItem("standings", "Standings", Icons.Default.Leaderboard, Icons.Outlined.Leaderboard),
                    NavigationItem("grid", "Grid", Icons.Default.Group, Icons.Outlined.Group),
                    NavigationItem("settings", "Profile", Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
                )

                // Only show bottom navigation on primary screens, hide on detail pages
                val primaryRoutes = items.map { it.route }
                val showBottomBar = currentDestination?.route in primaryRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                items.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_item_${item.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTiming = {
                                    // Navigate to Racing screen and select timing board
                                    navController.navigate("racing")
                                },
                                onNavigateToNewsDetail = { article ->
                                    navController.navigate("news_detail/${article.id}")
                                },
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                }
                            )
                        }

                        composable("racing") {
                            RacingScreen(
                                viewModel = viewModel,
                                onNavigateToRaceDetail = { race ->
                                    navController.navigate("race_detail/${race.round}")
                                },
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                }
                            )
                        }

                        composable("standings") {
                            StandingsScreen(
                                viewModel = viewModel,
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                },
                                onNavigateToTeamDetail = { teamId ->
                                    navController.navigate("team_detail/$teamId")
                                }
                            )
                        }

                        composable("grid") {
                            TeamsDriversScreen(
                                viewModel = viewModel,
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                },
                                onNavigateToTeamDetail = { teamId ->
                                    navController.navigate("team_detail/$teamId")
                                }
                            )
                        }

                        composable("settings") {
                            FavoritesSettingsScreen(
                                viewModel = viewModel,
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                },
                                onNavigateToTeamDetail = { teamId ->
                                    navController.navigate("team_detail/$teamId")
                                },
                                isDarkTheme = darkTheme,
                                onToggleTheme = { darkTheme = it }
                            )
                        }

                        composable(
                            route = "driver_detail/{driverId}",
                            arguments = listOf(navArgument("driverId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val driverId = backStackEntry.arguments?.getString("driverId") ?: ""
                            DriverDetailScreen(
                                driverId = driverId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "team_detail/{teamId}",
                            arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                            TeamDetailScreen(
                                teamId = teamId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToDriverDetail = { driverId ->
                                    navController.navigate("driver_detail/$driverId")
                                }
                            )
                        }

                        composable(
                            route = "race_detail/{round}",
                            arguments = listOf(navArgument("round") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val round = backStackEntry.arguments?.getInt("round") ?: 1
                            RaceDetailScreen(
                                round = round,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "news_detail/{newsId}",
                            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
                            NewsDetailScreen(
                                newsId = newsId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
