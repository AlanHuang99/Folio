package com.folio.reader.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.ui.screens.articles.ArticleListScreen
import com.folio.reader.ui.screens.feeds.FeedsScreen
import com.folio.reader.ui.screens.login.LoginScreen
import com.folio.reader.ui.screens.reader.ReaderScreen
import com.folio.reader.ui.screens.search.SearchScreen
import com.folio.reader.ui.screens.settings.SettingsScreen

/**
 * The single app-level Scaffold: a dynamic top bar + bottom navigation + a NavHost.
 * Window insets are owned here so child screens stay inset-agnostic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolioScaffold(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val unreadTotal by mainViewModel.unreadTotal.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }

    val isArticles = route?.startsWith("articles/") == true
    val isReader = route?.startsWith("reader/") == true
    val isSettings = route == Routes.SETTINGS
    val isSearch = route == Routes.SEARCH
    val isAddAccount = route == Routes.ADD_ACCOUNT
    val canGoBack = isArticles || isReader || isSettings || isSearch
    val title = when {
        route == FolioTab.Unread.route -> "Unread"
        route == FolioTab.Feeds.route -> "Feeds"
        route == FolioTab.Starred.route -> "Starred"
        isSettings -> "Settings"
        isSearch -> "Search"
        isArticles -> Uri.decode(backStackEntry?.arguments?.getString("title").orEmpty())
        isReader -> "Article"
        else -> "Folio"
    }

    Scaffold(
        topBar = {
            // The add-account route shows the login screen's own top bar.
            if (!isAddAccount) CenterAlignedTopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!canGoBack) {
                        IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { menuOpen = false; navController.navigate(Routes.SETTINGS) },
                                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!isReader && !isSettings && !isSearch && !isAddAccount) {
                NavigationBar {
                    FolioTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab == FolioTab.Unread && unreadTotal > 0) {
                                    BadgedBox(badge = {
                                        Badge { Text(if (unreadTotal > 99) "99+" else unreadTotal.toString()) }
                                    }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = FolioTab.Unread.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(FolioTab.Unread.route) {
                ArticleListScreen(
                    streamId = GReaderEndpoints.STREAM_READING_LIST,
                    excludeRead = true,
                    onOpenArticle = { navController.navigate(Routes.reader(it)) },
                )
            }
            composable(FolioTab.Starred.route) {
                ArticleListScreen(
                    streamId = GReaderEndpoints.STREAM_STARRED,
                    excludeRead = false,
                    onOpenArticle = { navController.navigate(Routes.reader(it)) },
                )
            }
            composable(FolioTab.Feeds.route) {
                FeedsScreen(onOpenStream = { streamId, streamTitle ->
                    navController.navigate(Routes.articles(streamId, streamTitle))
                })
            }
            composable(
                route = Routes.ARTICLES,
                arguments = listOf(
                    navArgument("stream") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                ),
            ) { entry ->
                val stream = Uri.decode(entry.arguments?.getString("stream").orEmpty())
                ArticleListScreen(
                    streamId = stream,
                    excludeRead = false,
                    onOpenArticle = { navController.navigate(Routes.reader(it)) },
                )
            }
            composable(
                route = Routes.READER,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                ReaderScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) })
            }
            composable(Routes.SEARCH) {
                SearchScreen(onOpenArticle = { navController.navigate(Routes.reader(it)) })
            }
            composable(Routes.ADD_ACCOUNT) {
                LoginScreen(onClose = { navController.popBackStack() })
            }
        }
    }
}
