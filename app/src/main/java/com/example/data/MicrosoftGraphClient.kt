package com.example.data

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.model.AcademicEmail
import com.example.model.EmailPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object MicrosoftGraphClient {
    private const val TAG = "MicrosoftGraph"
    const val CLIENT_ID = "8326f723-eb8a-4288-b346-9614eb8608c3"
    const val REDIRECT_URI = "unisync://auth/microsoft"
    private const val SCOPES = "openid profile email User.Read Mail.Read Mail.Send Mail.ReadWrite"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var codeVerifier: String? = null
    var accessToken: String? = null
        private set
    var signedInEmail: String? = null
        private set
    var signedInDisplayName: String? = null
        private set

    fun buildAuthorizeUri(): Uri {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        val verifier = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        codeVerifier = verifier
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/authorize").buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_mode", "query")
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
    }

    suspend fun exchangeCodeForToken(authCode: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val verifier = codeVerifier
                ?: return@withContext Result.failure(IllegalStateException("Missing PKCE verifier"))
            val body = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("scope", SCOPES)
                .add("code", authCode)
                .add("redirect_uri", REDIRECT_URI)
                .add("grant_type", "authorization_code")
                .add("code_verifier", verifier)
                .build()
            val req = Request.Builder()
                .url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .post(body)
                .build()
            val response = client.newCall(req).execute()
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("Microsoft token error (${response.code}): $responseText"))
            }
            val json = JSONObject(responseText)
            val token = json.optString("access_token")
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No access_token returned"))
            }
            accessToken = token
            fetchMeProfile(token)
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Microsoft OAuth exchange failed", e)
            Result.failure(e)
        }
    }

    private fun fetchMeProfile(token: String) {
        try {
            val req = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string().orEmpty())
                signedInDisplayName = json.optString("displayName").ifBlank { null }
                signedInEmail = json.optString("mail")
                    .ifBlank { json.optString("userPrincipalName") }
                    .ifBlank { null }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch /me profile", e)
        }
    }

    suspend fun fetchMessages(): Result<List<AcademicEmail>> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(IllegalStateException("Not signed in to Microsoft"))
        try {
            val req = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/messages?\$top=20&\$select=id,subject,bodyPreview,body,from,toRecipients,receivedDateTime,isRead,importance")
                .header("Authorization", "Bearer $token")
                .header("Prefer", "outlook.body-content-type=\"text\"")
                .get()
                .build()
            val res = client.newCall(req).execute()
            val bodyStr = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("Graph error (${res.code})"))
            }
            val arr = JSONObject(bodyStr).optJSONArray("value") ?: JSONArray()
            val list = mutableListOf<AcademicEmail>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val fromObj = item.optJSONObject("from")?.optJSONObject("emailAddress")
                val senderName = fromObj?.optString("name").orEmpty().ifBlank { "Outlook Sender" }
                val senderAddr = fromObj?.optString("address").orEmpty()
                val subject = item.optString("subject").ifBlank { "(No Subject)" }
                val preview = item.optString("bodyPreview").orEmpty()
                val rawBody = item.optJSONObject("body")?.optString("content").orEmpty()
                val fullBody = cleanHtmlOrText(rawBody).ifBlank { preview }
                val importance = item.optString("importance").lowercase()
                val priority = classifyPriority(subject, fullBody, importance)
                val courseTag = extractCourseTag("$subject $fullBody")
                list.add(
                    AcademicEmail(
                        id = item.optString("id"),
                        senderName = senderName,
                        senderEmail = senderAddr,
                        recipientEmail = signedInEmail.orEmpty(),
                        subject = subject,
                        preview = preview,
                        fullBody = fullBody,
                        receivedAtLabel = item.optString("receivedDateTime").take(16).replace("T", " "),
                        timestampMillis = System.currentTimeMillis() - (i * 600_000L),
                        priority = priority,
                        courseTag = courseTag,
                        unread = !item.optBoolean("isRead", true),
                        fromMicrosoftGraph = true
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Graph fetchMessages failed", e)
            Result.failure(e)
        }
    }

    suspend fun replyToMessage(messageId: String, comment: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(IllegalStateException("Not signed in to Microsoft"))
        try {
            val payload = JSONObject().put("comment", comment).toString()
            val req = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/messages/$messageId/reply")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("Graph reply failed (${res.code})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMail(toEmail: String, subject: String, bodyText: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(IllegalStateException("Not signed in to Microsoft"))
        try {
            val recipient = JSONObject().put("emailAddress", JSONObject().put("address", toEmail))
            val message = JSONObject()
                .put("subject", subject)
                .put("body", JSONObject().put("contentType", "Text").put("content", bodyText))
                .put("toRecipients", JSONArray().put(recipient))
            val payload = JSONObject().put("message", message).put("saveToSentItems", true).toString()
            val req = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/sendMail")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("Graph sendMail failed (${res.code})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearSession() {
        accessToken = null
        signedInEmail = null
        signedInDisplayName = null
    }

    private fun cleanHtmlOrText(raw: String): String {
        if (!raw.contains("<")) return raw.trim()
        return raw
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>|</div>|</li>"), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun classifyPriority(subject: String, body: String, importance: String): EmailPriority {
        val text = "$subject $body".lowercase()
        return when {
            importance == "high" && (text.contains("deadline") || text.contains("urgent") || text.contains("exam")) -> EmailPriority.CRITICAL
            text.contains("urgent") || text.contains("deadline") || text.contains("mta") || text.contains("tma") -> EmailPriority.CRITICAL
            importance == "high" || text.contains("exam") || text.contains("grade") || text.contains("clarify") -> EmailPriority.HIGH
            importance == "low" || text.contains("newsletter") || text.contains("fair") -> EmailPriority.LOW
            else -> EmailPriority.NORMAL
        }
    }

    private fun extractCourseTag(text: String): String {
        val match = Regex("\\b([A-Z]{1,4}\\d{3}(?:-[A-B]|[A-B])?)\\b").find(text)
        return match?.groupValues?.getOrNull(1) ?: "AOU"
    }
}
