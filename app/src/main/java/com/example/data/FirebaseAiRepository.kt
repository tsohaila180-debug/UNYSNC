package com.example.data

import android.util.Log
import com.example.model.AcademicEmail
import com.example.model.AppLanguage
import com.example.model.ReplyTone
import com.example.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

data class ExtractedDateItem(
    val title: String,
    val dateLabel: String,
    val courseCode: String,
    val category: String
)

class FirebaseAiRepository {

    companion object {
        private const val TAG = "UniSyncAiRepo"
    }

    private val defaultModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val extractSchema = Schema.obj(
        properties = mapOf(
            "events" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Event or deadline title"),
                        "dateLabel" to Schema.string(description = "Date and time found in the email"),
                        "courseCode" to Schema.string(description = "AOU course code such as TM351"),
                        "category" to Schema.enumeration(listOf("lecture", "exam", "assignment", "office_hour", "personal"))
                    )
                )
            )
        )
    )

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "subject" to Schema.string(description = "Translated email subject"),
            "body" to Schema.string(description = "Translated full email body preserving formatting and URLs")
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = extractSchema
            }
        )
    }

    private val translationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    suspend fun translateEmail(
        email: AcademicEmail,
        targetLanguage: AppLanguage
    ): Pair<String, String> {
        if (targetLanguage.code == "en") return email.subject to email.fullBody
        if (targetLanguage.code == "ar") {
            SampleAcademicData.arabicEmailTranslations[email.id]?.let { return it }
        }
        val langInstruction = if (targetLanguage.code == "ar") {
            "clean, respectful Modern Standard Arabic / Saudi academic phrasing (no Egyptian colloquialisms)"
        } else {
            "${targetLanguage.englishName} (${targetLanguage.nativeName})"
        }
        val prompt = """
            Translate the following Arab Open University academic email into $langInstruction.
            Preserve course codes (like TM351, M269), dates, and any https:// URLs accurately.
            
            Subject: ${email.subject}
            Body:
            ${email.fullBody}
        """.trimIndent()

        return try {
            val response = withTimeout(18_000L) {
                translationModel.generateContent(prompt)
            }
            val safe = sanitizeOrNull(response.text) ?: return buildLocalLanguageAdaptation(email, targetLanguage)
            val json = JSONObject(safe)
            val tSub = json.optString("subject").trim().ifBlank { email.subject }
            val tBody = json.optString("body").trim().ifBlank { email.fullBody }
            tSub to tBody
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Using local language adaptation for ${targetLanguage.code}", e)
            buildLocalLanguageAdaptation(email, targetLanguage)
        }
    }

    suspend fun summarizeEmail(
        email: AcademicEmail,
        languageCode: String = "en"
    ): String {
        val langObj = LocalizationData.find(languageCode)
        val langNote = if (languageCode == "ar") {
            "Respond in clean, formal Modern Standard Arabic / Saudi academic style."
        } else {
            "Respond in ${langObj.englishName}."
        }
        val prompt = """
            $langNote
            Summarize this Arab Open University academic email concisely in 3 bullet points:
            - Core purpose / announcement
            - Required actions or deliverables
            - Any deadlines, dates, or links
            
            Subject: ${email.subject}
            From: ${email.senderName} (${email.senderEmail})
            Body:
            ${email.fullBody}
        """.trimIndent()

        return try {
            val response = withTimeout(20_000L) {
                defaultModel.generateContent(prompt)
            }
            sanitizeOrNull(response.text) ?: buildExtractiveSummary(email, languageCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Using built-in academic summarizer", e)
            buildExtractiveSummary(email, languageCode)
        }
    }

    suspend fun generateSmartReply(
        email: AcademicEmail,
        tone: ReplyTone,
        role: UserRole,
        senderName: String,
        languageCode: String = "en"
    ): String {
        val roleDesc = if (role == UserRole.INSTRUCTOR) "an Arab Open University faculty instructor" else "an Arab Open University student"
        val langObj = LocalizationData.find(languageCode)
        val langNote = if (languageCode == "ar") {
            "Write the reply in respectful Modern Standard Arabic / Saudi academic phrasing (e.g. السلام عليكم ورحمة الله وبركاته)."
        } else {
            "Write the reply in ${langObj.englishName}."
        }
        val prompt = """
            $langNote
            Draft a ready-to-send email reply as $roleDesc named $senderName.
            Requested tone: ${tone.labelEn}.
            Original Subject: ${email.subject}
            Original Sender: ${email.senderName}
            Original Email:
            ${email.fullBody}
            
            Output ONLY the reply message text (greeting, concise response addressing key points, sign-off).
        """.trimIndent()

        return try {
            val response = withTimeout(20_000L) {
                defaultModel.generateContent(prompt)
            }
            sanitizeOrNull(response.text) ?: buildContextualReplyFallback(email, tone, role, senderName, languageCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Using built-in smart reply drafter", e)
            buildContextualReplyFallback(email, tone, role, senderName, languageCode)
        }
    }

    suspend fun extractDatesFromEmail(email: AcademicEmail): List<ExtractedDateItem> {
        return try {
            val prompt = """
                Extract any academic deadlines, lectures, exams, or office hours from this email.
                Course tag hint: ${email.courseTag}
                Subject: ${email.subject}
                Body:
                ${email.fullBody}
            """.trimIndent()
            val response = withTimeout(20_000L) {
                structuredModel.generateContent(prompt)
            }
            val safeText = sanitizeOrNull(response.text) ?: "{}"
            val json = JSONObject(safeText)
            val arr = json.optJSONArray("events") ?: JSONArray()
            val list = mutableListOf<ExtractedDateItem>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(
                    ExtractedDateItem(
                        title = item.optString("title").ifBlank { email.subject.take(60) },
                        dateLabel = item.optString("dateLabel").ifBlank { "Upcoming Term Date" },
                        courseCode = item.optString("courseCode").ifBlank { email.courseTag },
                        category = item.optString("category").ifBlank { "assignment" }
                    )
                )
            }
            if (list.isNotEmpty()) list else buildHeuristicDateExtraction(email)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Using built-in date parser", e)
            buildHeuristicDateExtraction(email)
        }
    }

    fun streamStudyAssistant(
        question: String,
        majorName: String,
        languageCode: String = "en"
    ): Flow<String> = flow {
        val langObj = LocalizationData.find(languageCode)
        val langNote = if (languageCode == "ar") {
            "Respond in clean, formal Modern Standard Arabic / Saudi academic style."
        } else {
            "Respond in ${langObj.englishName}."
        }
        val prompt = """
            $langNote
            You are an academic study coach for Arab Open University (AOU) students.
            Student Major: $majorName
            Question: $question
            
            Provide a clear, structured, encouraging explanation with practical AOU study steps (TMA, MTA, or course concepts).
        """.trimIndent()

        val buffer = StringBuilder()
        try {
            withTimeout(25_000L) {
                defaultModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty() && sanitizeOrNull(piece) != null) {
                        buffer.append(piece)
                        emit(buffer.toString())
                    }
                }
            }
            if (buffer.isBlank()) {
                emit(buildLocalStudyCoachAnswer(question, majorName, languageCode))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Using built-in study guide", e)
            emit(buildLocalStudyCoachAnswer(question, majorName, languageCode))
        }
    }

    private fun sanitizeOrNull(raw: String?): String? {
        val text = raw?.trim().orEmpty()
        if (text.isBlank()) return null
        val blockedTerms = listOf(
            "exceeded your current quota",
            "check your plan and billing",
            "billing details",
            "rate limit",
            "quota",
            "api key",
            "resource_exhausted",
            "permission_denied"
        )
        return if (blockedTerms.any { text.contains(it, ignoreCase = true) }) null else text
    }

    private fun buildLocalLanguageAdaptation(
        email: AcademicEmail,
        targetLanguage: AppLanguage
    ): Pair<String, String> {
        if (targetLanguage.code == "ar") {
            SampleAcademicData.arabicEmailTranslations[email.id]?.let { return it }
            val adaptedSubject = "[${email.courseTag}] ${email.subject}"
            val adaptedBody = """رسالة أكاديمية — الجامعة العربية المفتوحة (${email.courseTag})
المرسل: ${email.senderName}

${email.fullBody}"""
            return adaptedSubject to adaptedBody
        }
        return email.subject to email.fullBody
    }

    private fun buildExtractiveSummary(email: AcademicEmail, languageCode: String): String {
        val arabicPair = if (languageCode == "ar") SampleAcademicData.arabicEmailTranslations[email.id] else null
        val bodySource = arabicPair?.second ?: email.fullBody
        val subjectSource = arabicPair?.first ?: email.subject
        val lines = bodySource.lines()
            .map { it.trim() }
            .filter {
                it.length > 16 &&
                    !it.startsWith("Dear ", true) &&
                    !it.startsWith("Best", true) &&
                    !it.startsWith("Warm", true) &&
                    !it.startsWith("Regards", true) &&
                    !it.startsWith("مع ", true)
            }
        val keyLines = lines.filter {
            it.startsWith("-") || it.startsWith("1.") || it.startsWith("2.") ||
                it.contains("deadline", true) || it.contains("exam", true) ||
                it.contains("date", true) || it.contains("http", true) ||
                it.contains("موعد", true) || it.contains("اختبار", true) || it.contains("تسليم", true)
        }.ifEmpty { lines }.take(3)
        val bullets = if (keyLines.isNotEmpty()) {
            keyLines.joinToString("\n") { "• ${it.removePrefix("-").trim()}" }
        } else {
            "• $subjectSource"
        }
        val courseLabel = email.courseTag.ifBlank { "AOU" }
        return if (languageCode == "ar") {
            "ملخص الرسالة ($courseLabel):\n$bullets"
        } else {
            "Summary ($courseLabel):\n$bullets"
        }
    }

    private fun buildContextualReplyFallback(
        email: AcademicEmail,
        tone: ReplyTone,
        role: UserRole,
        senderName: String,
        languageCode: String
    ): String {
        val course = email.courseTag.ifBlank { "AOU" }
        if (languageCode == "ar") {
            val greeting = "السلام عليكم ورحمة الله وبركاته،"
            val signOff = if (role == UserRole.INSTRUCTOR) {
                "وتفضلوا بقبول فائق التقدير،\n$senderName\nعضو هيئة التدريس — الجامعة العربية المفتوحة"
            } else {
                "وتفضلوا بقبول وافر الاحترام،\n$senderName\nطالب في الجامعة العربية المفتوحة"
            }
            val middle = when (tone) {
                ReplyTone.FORMAL -> if (role == UserRole.INSTRUCTOR) {
                    "شكراً لتواصلكم بشأن مقرر $course. تم الاطلاع على رسالتكم بعناية، ونؤكد التزام الشعبة بالتعليمات والمعايير المنشورة على نظام LMS."
                } else {
                    "شكراً جزيلاً لكم على التوضيح بخصوص مقرر $course. تم الاطلاع على جميع التعليمات والمواعيد المحددة، وسأعمل على تنفيذ المطلوب قبل الموعد النهائي."
                }
                ReplyTone.FRIENDLY_CONCISE -> if (role == UserRole.INSTRUCTOR) {
                    "أهلاً بكم، شكراً لتواصلكم حول مقرر $course. يرجى اتباع الإرشادات المنشورة في بوابة المقرر، ولا تترددوا في طرح أي استفسار عبر المحادثة المباشرة."
                } else {
                    "شكراً جزيلاً على التحديث المهم لمقرر $course، التعليمات واضحة وسألتزم بالجدول المحدد بإذن الله."
                }
                ReplyTone.CLARIFICATION -> "شكراً لرسالتكم الكريمة بشأن مقرر $course. نرجو التكرم بتوضيح نقطة واحدة حول المتطلبات التفصيلية للتسليم لضمان الالتزام التام بمعايير التقييم."
            }
            return "$greeting\n\n$middle\n\n$signOff"
        }

        val greeting = "Dear ${email.senderName.substringBefore(" (")},"
        val signOff = if (role == UserRole.INSTRUCTOR) {
            "Best regards,\n$senderName\nFaculty Member — Arab Open University"
        } else {
            "Kind regards,\n$senderName\nAOU Student"
        }
        val middle = when (tone) {
            ReplyTone.FORMAL -> if (role == UserRole.INSTRUCTOR) {
                "Thank you for reaching out regarding $course. I have noted the details in your message (\"${email.subject}\") and confirm that all guidelines on the AOU LMS remain active. Please follow the posted rubric and submission checklist."
            } else {
                "Thank you very much for sharing the update regarding \"${email.subject}\" ($course). I have reviewed the instructions and dates carefully and will complete the required steps before the deadline."
            }
            ReplyTone.FRIENDLY_CONCISE -> if (role == UserRole.INSTRUCTOR) {
                "Thanks for your message about $course! Please follow the guidelines in the LMS post and reach out in Direct Chat if anything remains unclear."
            } else {
                "Thank you for the helpful update on $course! Everything is clear on my end, and I will follow up as scheduled."
            }
            ReplyTone.CLARIFICATION -> "Thank you for your message regarding \"${email.subject}\". Could you kindly clarify one point regarding the exact formatting/requirements for $course so everything aligns with the rubric?"
        }
        return "$greeting\n\n$middle\n\n$signOff"
    }

    private fun buildHeuristicDateExtraction(email: AcademicEmail): List<ExtractedDateItem> {
        val dateRegex = Regex("(?i)(?:nov|november|dec|december|jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|september|oct|october)\\s+\\d{1,2}(?:,\\s*\\d{4})?(?:\\s+at\\s+\\d{1,2}:\\d{2}\\s*(?:AM|PM))?")
        val matches = dateRegex.findAll(email.fullBody).map { it.value }.distinct().toList()
        return if (matches.isNotEmpty()) {
            matches.take(3).mapIndexed { index, dateLabel ->
                val cat = when {
                    email.fullBody.contains("exam", true) -> "exam"
                    email.fullBody.contains("clinic", true) || email.fullBody.contains("lecture", true) -> "lecture"
                    else -> "assignment"
                }
                ExtractedDateItem(
                    title = if (index == 0) email.subject.take(65) else "${email.courseTag} Scheduled Milestone",
                    dateLabel = dateLabel,
                    courseCode = email.courseTag,
                    category = cat
                )
            }
        } else {
            listOf(
                ExtractedDateItem(
                    title = email.subject.take(65),
                    dateLabel = "Extracted from Email",
                    courseCode = email.courseTag,
                    category = "assignment"
                )
            )
        }
    }

    private fun buildLocalStudyCoachAnswer(
        question: String,
        majorName: String,
        languageCode: String
    ): String {
        if (languageCode == "ar") {
            return """خطة مذاكرة مركّزة لتخصص **$majorName**:

1. **فهم المفاهيم الأساسية**: قسّم موضوع "$question" إلى تعريفات أساسية، وأمثلة محلولة، وأسئلة الاختبارات السابقة المتاحة في نظام LMS.
2. **الاستذكار النشط**: خصّص جلسة بومودورو لمدة 25 دقيقة لتلخيص الخطوات دون الرجوع للمذكرات، ثم طبّق على 3 مسائل تدريبية.
3. **مراجعة معايير التقييم**: تأكد دائماً من توافق إجاباتك في الواجب الدراسي (TMA) والاختبار النصفي (MTA) مع نموذج التقييم المعتمد وتوثيق المراجع بوضوح."""
        }
        return """Here is a focused AOU study breakdown for **$majorName**:

1. **Core Concept Focus**: Break "$question" into definitions, worked examples, and past MTA/TMA questions from your AOU LMS unit guide.
2. **Active Recall**: Spend one 25-minute Pomodoro block summarizing the key steps without notes, then test yourself on 3 sample exam problems.
3. **Submission Check**: Always align your answers with the AOU TMA/MTA marking rubric and cite references clearly."""
    }
}
