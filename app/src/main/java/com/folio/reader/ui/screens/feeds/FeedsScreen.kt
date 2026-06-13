package com.folio.reader.ui.screens.feeds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.repository.FeedNode

@Composable
fun FeedsScreen(
    onOpenStream: (streamId: String, title: String) -> Unit,
    viewModel: FeedsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        FeedsUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        is FeedsUiState.Error -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                s.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 16.dp)) {
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
                item(key = category.streamId) {
                    ListItem(
                        headlineContent = {
                            Text(category.label, style = MaterialTheme.typography.titleMedium)
                        },
                        leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                        trailingContent = { CountBadge(category.unreadCount) },
                        modifier = Modifier.clickable {
                            onOpenStream(category.streamId, category.label)
                        },
                    )
                }
                items(category.feeds, key = { it.streamId }) { feed ->
                    FeedRow(feed, indent = true, onOpenStream = onOpenStream)
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
                    FeedRow(feed, indent = true, onOpenStream = onOpenStream)
                }
            }
        }
    }
}

@Composable
private fun FeedRow(
    feed: FeedNode,
    indent: Boolean,
    onOpenStream: (String, String) -> Unit,
) {
    ListItem(
        headlineContent = { Text(feed.title) },
        leadingContent = { FeedIcon(feed.iconUrl) },
        trailingContent = { CountBadge(feed.unreadCount) },
        modifier = Modifier
            .clickable { onOpenStream(feed.streamId, feed.title) }
            .padding(start = if (indent) 16.dp else 0.dp),
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
