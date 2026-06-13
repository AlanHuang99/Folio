package com.folio.reader.ui.screens.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.folio.reader.data.repository.Article
import com.folio.reader.data.repository.ArticleBlock
import com.folio.reader.data.repository.ArticleHtmlParser

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(viewModel: ReaderViewModel = hiltViewModel()) {
    val ids = viewModel.ids
    val pagerState = rememberPagerState(initialPage = viewModel.initialIndex) { ids.size }
    val context = LocalContext.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            ids.getOrNull(page)?.let { viewModel.markReadOnSettle(it) }
        }
    }

    Column(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val id = ids[page]
            LaunchedEffect(id) { viewModel.ensureLoaded(id) }
            when (val article = viewModel.article(id)) {
                null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                else -> ArticlePage(article, viewModel.readerMode(id))
            }
        }

        val currentId = ids.getOrNull(pagerState.currentPage)
        val current = currentId?.let { viewModel.article(it) }
        val currentReaderState = currentId?.let { viewModel.readerMode(it) } ?: ReaderModeState.Off
        HorizontalDivider()
        ReaderActionBar(
            article = current,
            readerState = currentReaderState,
            onToggleReaderMode = { currentId?.let { viewModel.toggleReaderMode(it) } },
            onToggleStar = { currentId?.let { viewModel.toggleStar(it) } },
            onToggleRead = { currentId?.let { viewModel.toggleRead(it) } },
            onShare = { current?.let { shareArticle(context, it) } },
            onOpenInBrowser = { current?.url?.let { openUrl(context, it) } },
        )
    }
}

@Composable
private fun ArticlePage(article: Article, readerState: ReaderModeState) {
    val isLoading = readerState is ReaderModeState.Loading
    val html = (readerState as? ReaderModeState.Content)?.html ?: article.contentHtml
    val blocks = remember(html, isLoading) {
        if (isLoading) emptyList() else ArticleHtmlParser.parse(html)
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(article.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                metaLine(article),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
        }

        if (isLoading) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Loading full article…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            if (readerState is ReaderModeState.Failed) {
                item {
                    Text(
                        "Couldn't load the full article. Showing the feed summary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
            if (blocks.isEmpty()) {
                item {
                    Text(
                        article.excerpt.ifBlank { "No preview available. Open in browser to read the full article." },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(blocks) { block ->
                    when (block) {
                        is ArticleBlock.Text -> {
                            HtmlText(block.html, Modifier.fillMaxWidth())
                            Spacer(Modifier.height(12.dp))
                        }

                        is ArticleBlock.Image -> AsyncImage(
                            model = block.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ReaderActionBar(
    article: Article?,
    readerState: ReaderModeState,
    onToggleReaderMode: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleRead: () -> Unit,
    onShare: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    val starred = article?.isStarred == true
    val read = article?.isRead == true
    val readerActive = readerState is ReaderModeState.Content
    val readerLoading = readerState is ReaderModeState.Loading
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleReaderMode) {
            if (readerLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ChromeReaderMode,
                    contentDescription = if (readerActive) "Show feed summary" else "Reader mode",
                    tint = if (readerActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onToggleStar) {
            Icon(
                if (starred) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (starred) "Unstar" else "Star",
                tint = if (starred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onToggleRead) {
            Icon(
                if (read) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (read) "Mark unread" else "Mark read",
                tint = if (read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onOpenInBrowser) {
            Icon(Icons.Filled.OpenInBrowser, contentDescription = "Open in browser")
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
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
    return listOfNotNull(
        article.feedTitle?.takeIf { it.isNotBlank() },
        article.author?.takeIf { it.isNotBlank() },
        time.takeIf { it.isNotBlank() },
    ).joinToString("  ·  ")
}

private fun shareArticle(context: Context, article: Article) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, listOfNotNull(article.title, article.url).joinToString("\n"))
    }
    runCatching { context.startActivity(Intent.createChooser(send, null)) }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
