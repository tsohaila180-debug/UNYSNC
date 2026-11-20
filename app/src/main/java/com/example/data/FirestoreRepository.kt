package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CalendarAlarmEvent
import com.example.model.ChatMessageItem
import com.example.model.CourseProgressDoc
import com.example.model.CourseStatus
import com.example.model.FacultyMaterialItem
import com.example.model.ManualNoteItem
import com.example.model.UserProfileDoc
import com.example.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val currentUser = Firebase.auth.currentUser
    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.GET, path, userId)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String,
        role: UserRole,
        branch: String,
        majorId: String,
        languageCode: String
    ): Result<Unit> {
        var docPath = "users/unknown"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid)
            docPath = ref.path
            val snap = ref.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.ifBlank { "AOU User" }.take(100),
                    "email" to email.take(150),
                    "role" to role.key,
                    "aouBranch" to branch.take(80),
                    "majorId" to majorId.take(80),
                    "companionGender" to "girl",
                    "fontScale" to "medium",
                    "darkMode" to false,
                    "languageCode" to languageCode.take(16),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                ref.set(payload).await()
            } else {
                ref.update(
                    mapOf(
                        "role" to role.key,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(fields: Map<String, Any>): Result<Unit> {
        var docPath = "users/unknown"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid)
            docPath = ref.path
            val updateMap = fields.toMutableMap().apply {
                put("updatedAt", FieldValue.serverTimestamp())
            }
            ref.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeCourseProgress(userId: String): Flow<Map<String, CourseStatus>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/courseProgress"
        emitAll(
            db.collection("users").document(userId).collection("courseProgress")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CourseProgressDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .associate { it.courseCode to CourseStatus.fromKey(it.status) }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun setCourseStatus(majorId: String, courseCode: String, status: CourseStatus): Result<Unit> {
        var docPath = "users/unknown/courseProgress/$courseCode"
        return try {
            val uid = requireUserId()
            val cleanCode = courseCode.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            val ref = db.collection("users").document(uid).collection("courseProgress").document(cleanCode)
            docPath = ref.path
            val snap = ref.get().await()
            if (!snap.exists()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "courseCode" to cleanCode,
                        "majorId" to majorId.take(80),
                        "status" to status.key,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "majorId" to majorId.take(80),
                        "status" to status.key,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeCalendarEvents(userId: String): Flow<List<CalendarAlarmEvent>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/calendarEvents"
        emitAll(
            db.collection("users").document(userId).collection("calendarEvents")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CalendarAlarmEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.eventTimeMillis }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveCalendarEvent(event: CalendarAlarmEvent): Result<Unit> {
        var docPath = "users/unknown/calendarEvents"
        return try {
            val uid = requireUserId()
            val col = db.collection("users").document(uid).collection("calendarEvents")
            val ref = if (event.id.isNotBlank()) col.document(event.id) else col.document()
            docPath = ref.path
            val payload = mapOf(
                "userId" to uid,
                "title" to event.title.take(160),
                "courseCode" to event.courseCode.take(32),
                "category" to event.category,
                "eventTimeMillis" to event.eventTimeMillis,
                "alarmEnabled" to event.alarmEnabled,
                "ringtoneId" to event.ringtoneId.take(40),
                "source" to event.source.take(40),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleEventAlarm(eventId: String, enabled: Boolean): Result<Unit> {
        var docPath = "users/unknown/calendarEvents/$eventId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("calendarEvents").document(eventId)
            docPath = ref.path
            ref.update(
                mapOf(
                    "alarmEnabled" to enabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCalendarEvent(eventId: String): Result<Unit> {
        var docPath = "users/unknown/calendarEvents/$eventId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("calendarEvents").document(eventId)
            docPath = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observeManualNotes(userId: String): Flow<List<ManualNoteItem>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/manualNotes"
        emitAll(
            db.collection("users").document(userId).collection("manualNotes")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ManualNoteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun addManualNote(title: String, content: String, linkUrl: String, courseTag: String): Result<Unit> {
        var docPath = "users/unknown/manualNotes"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("manualNotes").document()
            docPath = ref.path
            val payload = buildMap<String, Any> {
                put("userId", uid)
                put("title", title.trim().take(160))
                put("content", content.trim().take(4000))
                if (linkUrl.isNotBlank()) put("linkUrl", linkUrl.trim().take(500))
                if (courseTag.isNotBlank()) put("courseTag", courseTag.trim().take(32))
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteManualNote(noteId: String): Result<Unit> {
        var docPath = "users/unknown/manualNotes/$noteId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("manualNotes").document(noteId)
            docPath = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observePublishedMaterials(userId: String): Flow<List<FacultyMaterialItem>> = flow {
        require(userId.isNotBlank())
        val path = "materials"
        emitAll(
            db.collection("materials")
                .whereEqualTo("published", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FacultyMaterialItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun publishFacultyMaterial(
        authorName: String,
        courseCode: String,
        title: String,
        description: String,
        resourceUrl: String,
        materialType: String
    ): Result<Unit> {
        val path = "materials"
        return try {
            val uid = requireUserId()
            val ref = db.collection("materials").document()
            val payload = buildMap<String, Any> {
                put("authorId", uid)
                put("authorName", authorName.ifBlank { "AOU Faculty" }.take(100))
                put("courseCode", courseCode.trim().uppercase().take(32))
                put("title", title.trim().take(180))
                if (description.isNotBlank()) put("description", description.trim().take(1000))
                if (resourceUrl.isNotBlank()) put("resourceUrl", resourceUrl.trim().take(600))
                put("materialType", materialType)
                put("published", true)
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteFacultyMaterial(materialId: String): Result<Unit> {
        val path = "materials/$materialId"
        return try {
            requireUserId()
            db.collection("materials").document(materialId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeGeneralChat(userId: String): Flow<List<ChatMessageItem>> = flow {
        require(userId.isNotBlank())
        val path = "chatMessages"
        emitAll(
            db.collection("chatMessages")
                .whereEqualTo("channel", "general")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessageItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeDirectChat(userId: String): Flow<List<ChatMessageItem>> = flow {
        require(userId.isNotBlank())
        val path = "chatMessages"
        emitAll(
            db.collection("chatMessages")
                .whereEqualTo("channel", "direct")
                .whereArrayContains("participants", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessageItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun sendChatMessage(
        senderName: String,
        senderRole: UserRole,
        channel: String,
        peerUid: String?,
        courseTag: String,
        text: String,
        attachmentBase64: String?
    ): Result<Unit> {
        val path = "chatMessages"
        return try {
            val uid = requireUserId()
            val ref = db.collection("chatMessages").document()
            val participants = if (channel == "direct") {
                listOfNotNull(uid, peerUid?.takeIf { it.isNotBlank() && it != uid }).distinct().take(2)
            } else emptyList()
            val payload = buildMap<String, Any> {
                put("senderId", uid)
                put("senderName", senderName.ifBlank { "AOU Member" }.take(100))
                put("senderRole", senderRole.key)
                put("channel", channel)
                put("participants", participants)
                if (courseTag.isNotBlank()) put("courseTag", courseTag.trim().take(32))
                put("text", text.trim().take(2000))
                if (!attachmentBase64.isNullOrBlank()) {
                    put("attachmentBase64", attachmentBase64.take(350_000))
                }
                put("createdAt", FieldValue.serverTimestamp())
            }
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private fun unwrapAndLog(error: Throwable, op: OperationType, path: String, expectedUid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
