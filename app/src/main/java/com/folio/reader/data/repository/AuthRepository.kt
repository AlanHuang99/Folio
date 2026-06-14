package com.folio.reader.data.repository

import com.folio.reader.data.Account
import com.folio.reader.data.AccountStore
import com.folio.reader.data.api.GReaderApi
import com.folio.reader.data.api.GReaderEndpoints
import com.folio.reader.data.api.ServerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LoginResult {
    data object Success : LoginResult
    data class Error(val message: String) : LoginResult
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: GReaderApi,
    private val accounts: AccountStore,
    private val session: ServerSession,
) {
    val isLoggedIn: Flow<Boolean> = accounts.activeAccount.map { it != null }
    val accountsFlow: Flow<List<Account>> = accounts.accounts
    val activeAccountFlow: Flow<Account?> = accounts.activeAccount

    /** Migrate any legacy session, then point the in-memory session at the active account. */
    suspend fun restoreSession() {
        accounts.migrateIfNeeded()
        applySession(accounts.activeAccount.first())
    }

    /**
     * Sign in and make the account active. Works both as the first login and as
     * "add another account" while already signed in. On failure the previously
     * active session is left intact.
     */
    suspend fun login(serverUrl: String, username: String, apiPassword: String): LoginResult {
        if (serverUrl.isBlank() || username.isBlank() || apiPassword.isBlank()) {
            return LoginResult.Error("Server, username, and password are required.")
        }
        val prevBaseUrl = session.baseUrl
        val prevToken = session.token
        // Route the login probe at the new server.
        session.baseUrl = GReaderEndpoints.greaderBaseUrl(serverUrl.trim())
        session.token = null
        session.writeToken = null

        val token = try {
            val response = api.clientLogin(username.trim(), apiPassword)
            if (!response.isSuccessful) {
                restoreSession(prevBaseUrl, prevToken)
                return LoginResult.Error(
                    if (response.code() == 401 || response.code() == 403) {
                        "Invalid username or API password."
                    } else {
                        "Server returned HTTP ${response.code()}."
                    }
                )
            }
            GReaderEndpoints.parseAuthToken(response.body()?.string().orEmpty())
        } catch (e: Exception) {
            restoreSession(prevBaseUrl, prevToken)
            return LoginResult.Error(e.message ?: "Could not reach the server.")
        } ?: run {
            restoreSession(prevBaseUrl, prevToken)
            return LoginResult.Error("Invalid username or API password.")
        }

        session.token = token
        val name = try {
            api.userInfo().userName
        } catch (e: Exception) {
            null
        } ?: username.trim()

        accounts.upsertAndActivate(serverUrl.trim(), name, token)
        return LoginResult.Success
    }

    /** Switch the active account and re-point the session at it. */
    suspend fun switchAccount(id: String) {
        accounts.setActive(id)
        applySession(accounts.activeAccount.first())
    }

    /** Remove an account; the session follows the new active account (or clears). */
    suspend fun removeAccount(id: String) {
        accounts.remove(id)
        applySession(accounts.activeAccount.first())
    }

    /** Sign out of the active account; if others remain, the first becomes active. */
    suspend fun logout() {
        accounts.activeAccount.first()?.let { accounts.remove(it.id) }
        applySession(accounts.activeAccount.first())
    }

    private fun applySession(account: Account?) {
        session.baseUrl = account?.let { GReaderEndpoints.greaderBaseUrl(it.serverUrl) }
        session.token = account?.token
        session.writeToken = null
    }

    private fun restoreSession(baseUrl: String?, token: String?) {
        session.baseUrl = baseUrl
        session.token = token
        session.writeToken = null
    }
}
