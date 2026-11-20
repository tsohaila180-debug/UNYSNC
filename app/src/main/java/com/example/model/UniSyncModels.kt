package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class UserRole(val key: String) {
    STUDENT("student"),
    INSTRUCTOR("instructor");

    companion object {
        fun fromKey(raw: String?): UserRole =
            entries.firstOrNull { it.key == raw } ?: STUDENT
    }
}

enum class FontScaleOption(val key: String, val labelEn: String, val labelAr: String) {
    MEDIUM("medium", "Medium", "متوسط"),
    LARGE("large", "Large", "كبير"),
    EXTRA_LARGE("xlarge", "Extra Large", "كبير جداً");

    companion object {
        fun fromKey(raw: String?): FontScaleOption =
            entries.firstOrNull { it.key == raw } ?: MEDIUM
    }
}

enum class EmailPriority(val rank: Int, val key: String, val labelEn: String, val labelAr: String) {
    CRITICAL(0, "critical", "Critical", "مهم جداً"),
    HIGH(1, "high", "High", "مهم"),
    NORMAL(2, "normal", "Normal", "عادي"),
    LOW(3, "low", "Low", "أقل أهمية");

    companion object {
        fun fromKey(raw: String?): EmailPriority =
            entries.firstOrNull { it.key == raw } ?: NORMAL
    }
}

enum class ReplyTone(val labelEn: String, val labelAr: String) {
    FORMAL("Formal", "رسمي"),
    FRIENDLY_CONCISE("Friendly & Concise", "ودي ومختصر"),
    CLARIFICATION("Clarification Request", "طلب توضيح")
}

enum class CourseStatus(val key: String, val labelEn: String, val labelAr: String) {
    COMPLETED("completed", "Completed", "مكتملة"),
    IN_PROGRESS("in_progress", "In Progress", "قيد الدراسة"),
    NEXT_TERM("next_term", "Next Term", "الترم القادم"),
    NOT_STARTED("not_started", "Not Started", "لم تبدأ بعد");

    companion object {
        fun fromKey(raw: String?): CourseStatus =
            entries.firstOrNull { it.key == raw } ?: NOT_STARTED
    }
}

enum class CourseCategory(val labelEn: String, val labelAr: String) {
    UNIVERSITY_REQ("University Req", "متطلب جامعة"),
    FACULTY_REQ("Faculty / Core", "متطلب كلية"),
    MAJOR_CORE("Major Core", "تخصص إجباري"),
    ELECTIVE_PROJECT("Project / Capstone", "مشروع التخرج")
}

data class CourseNode(
    val code: String,
    val title: String,
    val credits: Int,
    val stage: Int,
    val category: CourseCategory,
    val prerequisites: List<String> = emptyList(),
    val description: String = ""
)

data class MajorRoadmap(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val facultyEn: String,
    val courses: List<CourseNode>
)

data class AcademicEmail(
    val id: String,
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String,
    val subject: String,
    val preview: String,
    val fullBody: String,
    val receivedAtLabel: String,
    val timestampMillis: Long,
    val priority: EmailPriority,
    val courseTag: String,
    val unread: Boolean,
    val replied: Boolean = false,
    val viewed: Boolean = false,
    val fromMicrosoftGraph: Boolean = false
)

data class RepeatedQuestionCluster(
    val id: String,
    val topic: String,
    val courseCode: String,
    val askCount: Int,
    val summary: String,
    val studentEmails: List<String>
)

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "student",
    val aouBranch: String = "Riyadh (الرياض) — Saudi Arabia",
    val majorId: String = "cs",
    val companionGender: String = "girl",
    val fontScale: String = "medium",
    val darkMode: Boolean = false,
    val languageCode: String = "en",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CourseProgressDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseCode: String = "",
    val majorId: String = "",
    val status: String = "not_started",
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CalendarAlarmEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val courseCode: String = "",
    val category: String = "lecture",
    val eventTimeMillis: Long = 0L,
    val alarmEnabled: Boolean = true,
    val ringtoneId: String = "chime",
    val source: String = "manual",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ManualNoteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val linkUrl: String = "",
    val courseTag: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FacultyMaterialItem(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val courseCode: String = "",
    val title: String = "",
    val description: String = "",
    val resourceUrl: String = "",
    val materialType: String = "slides",
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessageItem(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "student",
    val channel: String = "general",
    val participants: List<String> = emptyList(),
    val courseTag: String = "",
    val text: String = "",
    val attachmentBase64: String? = null,
    val createdAt: Timestamp? = null
)

data class FreshmanQaItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

data class VoiceNoteItem(
    val id: String,
    val title: String,
    val filePath: String,
    val durationSec: Int,
    val createdAtMillis: Long
)

data class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val rtl: Boolean = false
)

data class PlatformDownloadOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val badge: String,
    val instructions: String,
    val storeUrl: String? = null
)

data class StudyMessage(
    val id: String,
    val senderIsUser: Boolean,
    val text: String
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val text: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}
