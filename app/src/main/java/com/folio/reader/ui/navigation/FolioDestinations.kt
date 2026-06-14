package com.folio.reader.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/** Bottom-navigation tabs. */
enum class FolioTab(val route: String, val label: String, val icon: ImageVector) {
    Unread("unread", "Unread", Icons.Filled.Inbox),
    Feeds("feeds", "Feeds", Icons.Filled.Folder),
    Starred("starred", "Starred", Icons.Filled.Star),
}

object Routes {
    // streamId, title, and item id carry slashes/spaces, so they are URL-encoded.
    const val ARTICLES = "articles/{stream}/{title}"
    const val READER = "reader/{id}"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val ADD_ACCOUNT = "add_account"

    fun articles(streamId: String, title: String): String =
        "articles/${Uri.encode(streamId)}/${Uri.encode(title)}"

    fun reader(itemId: String): String = "reader/${Uri.encode(itemId)}"
}
