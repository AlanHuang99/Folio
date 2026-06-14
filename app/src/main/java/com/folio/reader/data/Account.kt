package com.folio.reader.data

import com.folio.reader.data.api.GReaderEndpoints

/** A signed-in FreshRSS account. [token] is the GoogleLogin auth token. */
data class Account(
    val id: String,
    val serverUrl: String,
    val userName: String,
    val token: String,
)

/**
 * Pure operations over the account list, kept Android-free for unit testing.
 * The account id is derived from the normalized server base URL plus the
 * username, so re-logging into the same account updates it instead of
 * creating a duplicate, while the same person on two servers stays distinct.
 */
object Accounts {
    fun idFor(serverUrl: String, userName: String): String =
        GReaderEndpoints.greaderBaseUrl(serverUrl) + "|" + userName.trim()

    /** Add [account], replacing any existing entry with the same id. Order is preserved. */
    fun upsert(list: List<Account>, account: Account): List<Account> {
        val index = list.indexOfFirst { it.id == account.id }
        return if (index >= 0) list.toMutableList().also { it[index] = account }
        else list + account
    }

    fun remove(list: List<Account>, id: String): List<Account> = list.filterNot { it.id == id }

    /** The active account: the one matching [activeId], else the first, else null. */
    fun resolveActive(list: List<Account>, activeId: String?): Account? =
        list.firstOrNull { it.id == activeId } ?: list.firstOrNull()
}
