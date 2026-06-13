package com.folio.reader.ui.screens.articles

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.folio.reader.data.repository.Article

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleListScreen(
    streamId: String,
    excludeRead: Boolean,
    onOpenArticle: (String) -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel(),
) {
    LaunchedEffect(streamId, excludeRead) { viewModel.start(streamId, excludeRead) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullState = rememberPullRefreshState(refreshing = state.refreshing, onRefresh = viewModel::refresh)

    Box(Modifier.fillMaxSize().pullRefresh(pullState)) {
        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

            state.error != null && state.articles.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
            }

            state.articles.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No articles here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> {
                val listState = rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(state.articles, key = { it.id }) { article ->
                        ArticleRow(
                            article = article,
                            onOpen = { viewModel.openArticle(article); onOpenArticle(article.id) },
                            onToggleStar = { viewModel.toggleStar(article) },
                            onToggleRead = { viewModel.toggleRead(article) },
                        )
                        HorizontalDivider()
                    }
                    if (state.loadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }

                val shouldLoadMore by remember {
                    derivedStateOf {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        last >= listState.layoutInfo.totalItemsCount - 5
                    }
                }
                LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }
            }
        }
        PullRefreshIndicator(state.refreshing, pullState, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun ArticleRow(
    article: Article,
    onOpen: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleRead: () -> Unit,
) {
    val titleColor = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (!article.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = article.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                article.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (article.isRead) FontWeight.Normal else FontWeight.Medium,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(2.dp))
            Text(
                metaLine(article),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.excerpt.isNotBlank()) {
                Spacer(Modifier.size(2.dp))
                Text(
                    article.excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onToggleStar) {
                Icon(
                    if (article.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (article.isStarred) "Unstar" else "Star",
                    tint = if (article.isStarred) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onToggleRead) {
                Icon(
                    if (article.isRead) Icons.Outlined.Circle else Icons.Filled.Circle,
                    contentDescription = if (article.isRead) "Mark unread" else "Mark read",
                    modifier = Modifier.size(14.dp),
                    tint = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun metaLine(article: Article): String {
    val time = if (article.publishedSec > 0) {
        DateUtils.getRelativeTimeSpanString(
            article.publishedSec * 1000,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    } else {
        ""
    }
    return listOfNotNull(article.feedTitle?.takeIf { it.isNotBlank() }, time.takeIf { it.isNotBlank() })
        .joinToString("  ·  ")
}
