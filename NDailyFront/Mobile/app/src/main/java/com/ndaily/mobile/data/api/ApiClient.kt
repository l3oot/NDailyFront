package com.ndaily.mobile.data.api

import com.ndaily.mobile.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ApiClient(private val tokens: TokenStore) {
    private val refreshMutex = Mutex()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun get(path: String, query: Map<String, String> = emptyMap(), auth: Boolean = true) =
        call("GET", path, query = query, auth = auth)

    suspend fun post(path: String, body: JSONObject? = null, auth: Boolean = true) =
        call("POST", path, body = body, auth = auth)

    suspend fun put(path: String, body: JSONObject? = null, auth: Boolean = true) =
        call("PUT", path, body = body, auth = auth)

    suspend fun delete(path: String, auth: Boolean = true) =
        call("DELETE", path, auth = auth)

    private suspend fun call(
        method: String,
        path: String,
        query: Map<String, String> = emptyMap(),
        body: JSONObject? = null,
        auth: Boolean,
        retried: Boolean = false,
    ): ApiResult = withContext(Dispatchers.IO) {
        val first = execute(method, path, query, body, auth)
        if (first.status != 401 || !auth || retried || path in SKIP_REFRESH) return@withContext first
        val refreshed = refreshMutex.withLock { refreshSession() }
        if (!refreshed) return@withContext first
        execute(method, path, query, body, auth = true)
    }

    private fun execute(
        method: String,
        path: String,
        query: Map<String, String>,
        body: JSONObject?,
        auth: Boolean,
    ): ApiResult {
        val url = buildUrl(path, query)
        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "NDaily-Android")
        if (auth && tokens.accessToken.isNotBlank()) {
            builder.header("Authorization", "Bearer ${tokens.accessToken}")
        }
        val payload = body?.toString()
        when (method) {
            "POST" -> builder.post((payload ?: "{}").toRequestBody(jsonType))
            "PUT" -> builder.put((payload ?: "{}").toRequestBody(jsonType))
            "PATCH" -> builder.patch((payload ?: "{}").toRequestBody(jsonType))
            "DELETE" -> builder.delete()
            else -> builder.get()
        }
        return try {
            http.newCall(builder.build()).execute().use { res ->
                val text = res.body?.string().orEmpty()
                val json = runCatching { JSONObject(text) }.getOrElse {
                    JSONObject()
                        .put("isSuccess", false)
                        .put("errorMessage", if (text.isBlank()) "Empty response" else "Invalid response")
                        .put("data", JSONObject.NULL)
                        .put("errorType", 5)
                }
                ApiResult(res.code, json)
            }
        } catch (error: Exception) {
            ApiResult(
                0,
                JSONObject()
                    .put("isSuccess", false)
                    .put("errorMessage", error.message ?: "Network error")
                    .put("data", JSONObject.NULL)
                    .put("errorType", 5),
            )
        }
    }

    private fun refreshSession(): Boolean {
        val refresh = tokens.refreshToken
        if (refresh.isBlank()) return false
        val result = execute(
            "POST",
            "/api/v1/auth/refreshtoken",
            emptyMap(),
            JSONObject().put("refreshToken", refresh),
            auth = false,
        )
        val data = result.obj()
        val access = data?.optString("token").orEmpty()
        val nextRefresh = data?.optString("refreshToken").orEmpty()
        val authUuid = data?.optString("authUuid").orEmpty().ifBlank { tokens.authUuid }
        if (!result.ok || access.isBlank() || nextRefresh.isBlank()) {
            if (result.status == 401 || result.status == 403) tokens.clear()
            return false
        }
        tokens.save(access, nextRefresh, authUuid)
        return true
    }

    companion object {
        val BASE_URL: String = BuildConfig.API_BASE_URL.trimEnd('/')
        private val SKIP_REFRESH = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/google",
            "/api/v1/auth/register",
            "/api/v1/auth/dev-login",
            "/api/v1/auth/send-otp",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/refreshtoken",
            "/api/v1/auth/logout",
        )

        private fun buildUrl(path: String, query: Map<String, String>): String {
            val encoded = query.entries.joinToString("&") { (key, value) ->
                "${URLEncoder.encode(key, Charsets.UTF_8.name())}=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
            }
            return if (encoded.isBlank()) "$BASE_URL$path" else "$BASE_URL$path?$encoded"
        }
    }
}

data class ApiResult(val status: Int, val json: JSONObject) {
    val ok: Boolean get() = json.optBoolean("isSuccess")
    val errorType: Int get() = json.optInt("errorType")
    val message: String
        get() = json.optString("errorMessage").ifBlank { json.optString("message") }

    fun obj(): JSONObject? = json.optJSONObject("data")
    fun arr(): JSONArray? = json.optJSONArray("data")
    fun str(key: String): String = obj()?.optString(key).orEmpty()
}
