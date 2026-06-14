package com.folio.reader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folio.reader.ui.navigation.FolioScaffold
import com.folio.reader.ui.screens.login.LoginScreen

/** Top-level auth gate: shows login or the main app based on session state. */
@Composable
fun FolioRoot(viewModel: RootViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    when (authState) {
        AuthState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        AuthState.LoggedOut -> LoginScreen()
        AuthState.LoggedIn -> {
            // Re-key on the active account so switching tears down and rebuilds
            // every screen/view-model with the new account's data.
            val accountId by viewModel.activeAccountId.collectAsStateWithLifecycle()
            key(accountId) { FolioScaffold() }
        }
    }
}
