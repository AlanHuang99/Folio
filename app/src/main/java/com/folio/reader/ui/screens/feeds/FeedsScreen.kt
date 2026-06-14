package com.folio.reader.ui.screens.feeds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.repository.FeedNode

/** Which management dialog is currently open. */
private sealed interface FeedDialog {
    data object AddFeed : FeedDialog
    data class RenameFeed(val streamId: String, val current: String) : FeedDialog
    data class MoveFeed(val streamId: String, val title: String, val currentCategory: String?) : FeedDialog
    data class ConfirmUnsubscribe(val streamId: String, val title: String) : FeedDialog
    data class RenameCategory(val name: String) : FeedDialog
    data class ConfirmDeleteCategory(val name: String) : FeedDialog
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedsScreen(
    onOpenStream: (streamId: String, title: String) -> Unit,
    viewModel: FeedsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val collapsed by viewModel.collapsedCategories.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<FeedDialog?>(null) }

    // Re-fetch when returning to this tab (so feeds/folders changed elsewhere appear),
    // but not on the very first resume — init already loaded.
    var firstResume by rememberSaveable { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (firstResume) firstResume = false else viewModel.refresh()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val pullState = rememberPullRefreshState(refreshing = refreshing, onRefresh = viewModel::refresh)

    Box(Modifier.fillMaxSize().pullRefresh(pullState)) {
        when (val s = state) {
            FeedsUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            is FeedsUiState.Error -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Button(onClick = viewModel::load, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }

            is FeedsUiState.Content -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    ListItem(
                        headlineContent = { Text("All articles") },
                        leadingContent = { Icon(Icons.Filled.Inbox, contentDescription = null) },
                        trailingContent = { CountBadge(s.tree.totalUnread) },
                        modifier = Modifier.clickable {
                            onOpenStream(GReaderEndpoints.STREAM_READING_LIST, "All articles")
                        },
                    )
                    HorizontalDivider()
                }

                s.tree.categories.forEach { category ->
                    val isCollapsed = category.label in collapsed
                    item(key = category.streamId) {
                        CategoryHeader(
                            label = category.label,
                            unread = category.unreadCount,
                            collapsed = isCollapsed,
                            onToggleCollapse = { viewModel.toggleCategoryCollapsed(category.label) },
                            onOpen = { onOpenStream(category.streamId, category.label) },
                            onRename = { dialog = FeedDialog.RenameCategory(category.label) },
                            onDelete = { dialog = FeedDialog.ConfirmDeleteCategory(category.label) },
                        )
                    }
                    if (!isCollapsed) {
                        items(category.feeds, key = { it.streamId }) { feed ->
                            FeedRow(
                                feed = feed,
                                currentCategory = category.label,
                                onOpenStream = onOpenStream,
                                onRename = { dialog = FeedDialog.RenameFeed(feed.streamId, feed.title) },
                                onMove = { dialog = FeedDialog.MoveFeed(feed.streamId, feed.title, category.label) },
                                onUnsubscribe = { dialog = FeedDialog.ConfirmUnsubscribe(feed.streamId, feed.title) },
                            )
                        }
                    }
                }

                if (s.tree.uncategorized.isNotEmpty()) {
                    item {
                        ListItem(
                            headlineContent = {
                                Text("Uncategorized", style = MaterialTheme.typography.titleMedium)
                            },
                            leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                        )
                    }
                    items(s.tree.uncategorized, key = { it.streamId }) { feed ->
                        FeedRow(
                            feed = feed,
                            currentCategory = null,
                            onOpenStream = onOpenStream,
                            onRename = { dialog = FeedDialog.RenameFeed(feed.streamId, feed.title) },
                            onMove = { dialog = FeedDialog.MoveFeed(feed.streamId, feed.title, null) },
                            onUnsubscribe = { dialog = FeedDialog.ConfirmUnsubscribe(feed.streamId, feed.title) },
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(refreshing, pullState, Modifier.align(Alignment.TopCenter))

        ExtendedFloatingActionButton(
            text = { Text("Add feed") },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            onClick = { dialog = FeedDialog.AddFeed },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )

        SnackbarHost(snackbarHost, Modifier.align(Alignment.BottomCenter))
    }

    when (val d = dialog) {
        null -> Unit
        FeedDialog.AddFeed -> AddFeedDialog(
            categories = viewModel.categoryNames(),
            onConfirm = { url, category -> viewModel.addFeed(url, category); dialog = null },
            onDismiss = { dialog = null },
        )
        is FeedDialog.RenameFeed -> TextEntryDialog(
            title = "Rename feed",
            initial = d.current,
            confirmLabel = "Rename",
            onConfirm = { viewModel.renameFeed(d.streamId, it); dialog = null },
            onDismiss = { dialog = null },
        )
        is FeedDialog.MoveFeed -> MoveFeedDialog(
            title = d.title,
            categories = viewModel.categoryNames(),
            currentCategory = d.currentCategory,
            onConfirm = { target -> viewModel.moveFeed(d.streamId, d.currentCategory, target); dialog = null },
            onDismiss = { dialog = null },
        )
        is FeedDialog.ConfirmUnsubscribe -> ConfirmDialog(
            title = "Unsubscribe?",
            text = "Unsubscribe from \"${d.title}\"? This removes it from your account.",
            confirmLabel = "Unsubscribe",
            onConfirm = { viewModel.unsubscribe(d.streamId, d.title); dialog = null },
            onDismiss = { dialog = null },
        )
        is FeedDialog.RenameCategory -> TextEntryDialog(
            title = "Rename folder",
            initial = d.name,
            confirmLabel = "Rename",
            onConfirm = { viewModel.renameCategory(d.name, it); dialog = null },
            onDismiss = { dialog = null },
        )
        is FeedDialog.ConfirmDeleteCategory -> ConfirmDialog(
            title = "Delete folder?",
            text = "Delete \"${d.name}\"? Its feeds become uncategorized (they are not unsubscribed).",
            confirmLabel = "Delete",
            onConfirm = { viewModel.deleteCategory(d.name); dialog = null },
            onDismiss = { dialog = null },
        )
    }
}

@Composable
private fun CategoryHeader(
    label: String,
    unread: Int,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.titleMedium) },
        leadingContent = {
            IconButton(onClick = onToggleCollapse) {
                Icon(
                    if (collapsed) Icons.Filled.KeyboardArrowRight else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (collapsed) "Expand $label" else "Collapse $label",
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountBadge(unread)
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Folder options")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Rename folder") },
                            onClick = { menu = false; onRename() })
                        DropdownMenuItem(text = { Text("Delete folder") },
                            onClick = { menu = false; onDelete() })
                    }
                }
            }
        },
        modifier = Modifier.clickable(onClick = onOpen),
    )
}

@Composable
private fun FeedRow(
    feed: FeedNode,
    currentCategory: String?,
    onOpenStream: (String, String) -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(feed.title) },
        leadingContent = { FeedIcon(feed.iconUrl) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountBadge(feed.unreadCount)
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Feed options")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Rename") },
                            onClick = { menu = false; onRename() })
                        DropdownMenuItem(text = { Text("Move to folder") },
                            onClick = { menu = false; onMove() })
                        DropdownMenuItem(text = { Text("Unsubscribe") },
                            onClick = { menu = false; onUnsubscribe() })
                    }
                }
            }
        },
        modifier = Modifier
            .clickable { onOpenStream(feed.streamId, feed.title) }
            .padding(start = 16.dp),
    )
}

@Composable
private fun AddFeedDialog(
    categories: List<String>,
    onConfirm: (url: String, category: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add feed") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Feed or site URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (categories.isNotEmpty()) {
                    Text(
                        "Folder",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    )
                    CategoryChoice(
                        categories = categories,
                        selected = selected,
                        noneLabel = "No folder",
                        onSelect = { selected = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(url, selected) }, enabled = url.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MoveFeedDialog(
    title: String,
    categories: List<String>,
    currentCategory: String?,
    onConfirm: (target: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(currentCategory) }
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move \"$title\"") },
        text = {
            Column {
                CategoryChoice(
                    categories = categories,
                    selected = selected,
                    noneLabel = "No folder",
                    onSelect = { selected = it; newName = "" },
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("or new folder") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(if (newName.isNotBlank()) newName.trim() else selected)
            }) { Text("Move") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** A vertical radio list of categories plus a "none" option. */
@Composable
private fun CategoryChoice(
    categories: List<String>,
    selected: String?,
    noneLabel: String,
    onSelect: (String?) -> Unit,
) {
    Column {
        ChoiceRow(label = noneLabel, isSelected = selected == null) { onSelect(null) }
        categories.forEach { category ->
            ChoiceRow(label = category, isSelected = selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun ChoiceRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected = isSelected, onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun TextEntryDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FeedIcon(iconUrl: String?) {
    val modifier = Modifier.size(24.dp)
    if (iconUrl.isNullOrBlank()) {
        Icon(
            Icons.Filled.RssFeed,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = modifier,
            error = rememberVectorPainter(Icons.Filled.RssFeed),
        )
    }
}

@Composable
private fun CountBadge(count: Int) {
    if (count > 0) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
