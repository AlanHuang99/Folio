package com.folio.reader.data.repository

import com.folio.reader.data.SettingsStore
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
    private val settings: SettingsStore,
    private val session: ServerSession,
) {
    val isLoggedIn: Flow<Boolean> = settings.authToken.map { !it.isNullOrBlank() }

    /** Populate the in-memory session from persisted settings. Call on app start. */
    suspend fun restoreSession() {
        val url = settings.serverUrl.first()
        val token = settings.authToken.first()
        if (!url.isNullOrBlank() && !token.isNullOrBlank()) {
            session.baseUrl = GReaderEndpoints.greaderBaseUrl(url)
            session.token = token
        }
    }

    suspend fun login(serverUrl: String, username: String, apiPassword: String): LoginResult {
        if (serverUrl.isBlank() || username.isBlank() || apiPassword.isBlank()) {
            return LoginResult.Error("Server, username, and password are required.")
        }
        // Route subsequent calls to this server; clear any stale token first.
        session.baseUrl = GReaderEndpoints.greaderBaseUrl(serverUrl.trim())
        session.token = null

        val token = try {
            val response = api.clientLogin(username.trim(), apiPassword)
            if (!response.isSuccessful) {
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
            return LoginResult.Error(e.message ?: "Could not reach the server.")
        } ?: return LoginResult.Error("Invalid username or API password.")

        session.token = token
        // Validate the token and fetch the display name (best-effort).
        val name = try {
            api.userInfo().userName
        } catch (e: Exception) {
            null
        } ?: username.trim()

        settings.saveSession(serverUrl.trim(), token, name)
        return LoginResult.Success
    }

    suspend fun logout() {
        settings.clear()
        session.token = null
        session.baseUrl = null
    }
}
