package com.folio.reader.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.folio.reader.ui.screens.articles.ArticlesScreen
import com.folio.reader.ui.screens.feeds.FeedsScreen

/**
 * The single app-level Scaffold: top bar + bottom navigation + a NavHost. Window
 * insets are owned here so child screens stay inset-agnostic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolioScaffold(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Folio") },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            onClick = { menuOpen = false; mainViewModel.signOut() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                FolioTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = FolioTab.Feeds.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(FolioTab.Unread.route) { ArticlesScreen(title = "Unread") }
            composable(FolioTab.Starred.route) { ArticlesScreen(title = "Starred") }
            composable(FolioTab.Feeds.route) {
                FeedsScreen(onOpenStream = { streamId, title ->
                    navController.navigate(Routes.articles(streamId, title))
                })
            }
            composable(
                route = Routes.ARTICLES,
                arguments = listOf(
                    navArgument("stream") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                ),
            ) { entry ->
                val title = Uri.decode(entry.arguments?.getString("title").orEmpty())
                ArticlesScreen(title = title)
            }
        }
    }
}
