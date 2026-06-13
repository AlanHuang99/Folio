package com.folio.reader.data.api

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory holder for the active server base URL + auth token, read by the
 * OkHttp interceptors on every request. Populated by AuthRepository on login and
 * on app start (from persisted settings).
 */
@Singleton
class ServerSession @Inject constructor() {
    @Volatile
    var baseUrl: String? = null // e.g. http://alan-mint:8280/api/greader.php/

    @Volatile
    var token: String? = null
}
