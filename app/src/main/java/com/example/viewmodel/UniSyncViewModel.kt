package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlarmAudioController
import com.example.data.AouRoadmapsData
import com.example.data.FirebaseAiRepository
import com.example.data.FirestoreRepository
import com.example.data.LocalUserSettings
import com.example.data.LocalizationData
import com.example.data.MicrosoftGraphClient
import com.example.data.PreferencesRepository
import com.example.data.SampleAcademicData
import com.example.data.VoiceNoteRecorder
import com.example.model.AcademicEmail
import com.example.model.AiUiState
import com.example.model.CalendarAlarmEvent
import com.example.model.ChatMessageItem
import com.example.model.CourseStatus
import com.example.model.EmailPriority
import com.example.model.FacultyMaterialItem
import com.example.model.FontScaleOption
import com.example.model.ManualNoteItem
import com.example.model.RepeatedQuestionCluster
import com.example.model.ReplyTone
import com.example.model.StudyMessage
import com.example.model.UserRole
import com.example.model.VoiceNoteItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

sealed interface SessionMode {
    data object SignedOut : SessionMode
    data class GoogleUser(val uid: String, val displayName: String, val email: String) : SessionMode
    data class MicrosoftUser(val displayName: String, val email: String) : SessionMode
    data class DemoUser(val displayName: String, val email: String) : SessionMode
}

data class UniSyncUiState(
    val session: SessionMode = SessionMode.SignedOut,
    val settings: LocalUserSettings = LocalUserSettings(),
    val statusBanner: String? = null,
    val emails: List<AcademicEmail> = SampleAcademicData.sampleEmails(UserRole.STUDENT),
    val priorityFilter: EmailPriority? = null,
    val sortByPriority: Boolean = true,
    val selectedEmailId: String? = null,
    val emailSummaryState: AiUiState = AiUiState.Idle,
    val smartReplyDraft: String = "",
    val smartReplyLoading: Boolean = false,
    val selectedReplyTone: ReplyTone = ReplyTone.FORMAL,
    val repeatedQuestions: List<RepeatedQuestionCluster> = SampleAcademicData.repeatedStudentQuestions,
    val answeredQuestionIds: Set<String> = emptySet(),
    val answeredQuestionReplies: Map<String, String> = emptyMap(),
    val sentEmailReplies: Map<String, String> = emptyMap(),
    val translatedEmails: Map<String, Pair<String, String>> = emptyMap(),
    val calendarEvents: List<CalendarAlarmEvent> = SampleAcademicData.defaultCalendarEvents(UserRole.STUDENT),
    val nowMillis: Long = System.currentTimeMillis(),
    val ringingEvent: CalendarAlarmEvent? = null,
    val triggeredEventIds: Set<String> = emptySet(),
    val courseStatuses: Map<String, CourseStatus> = emptyMap(),
    val manualNotes: List<ManualNoteItem> = emptyList(),
    val facultyMaterials: List<FacultyMaterialItem> = SampleAcademicData.defaultFacultyMaterials,
    val chatMessages: List<ChatMessageItem> = SampleAcademicData.defaultChatMessages,
    val studyMessages: List<StudyMessage> = emptyList(),
    val studyStreaming: Boolean = false,
    val freshmanAiState: AiUiState = AiUiState.Idle,
    val voiceNotes: List<VoiceNoteItem> = emptyList(),
    val recordingVoice: Boolean = false,
    val playingVoiceNoteId: String? = null,
    val pomodoroDurationMinutes: Int = 25,
    val pomodoroSecondsRemaining: Int = 25 * 60,
    val pomodoroRunning: Boolean = false,
    val pomodoroIsBreak: Boolean = false,
    val milestoneAwardMinutes: Int? = null
)

class UniSyncViewModel(
    private val preferencesRepo: PreferencesRepository,
    private val firestoreRepo: FirestoreRepository,
    private val aiRepo: FirebaseAiRepository,
    private val alarmAudio: AlarmAudioController,
    private val voiceRecorder: VoiceNoteRecorder
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniSyncUiState())
    val uiState: StateFlow<UniSyncUiState> = _uiState.asStateFlow()

    val settingsState: StateFlow<LocalUserSettings> = preferencesRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalUserSettings())

    private var clockJob: Job? = null
    private var pomodoroJob: Job? = null
    private var firestoreListenersJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepo.settingsFlow.collect { local ->
                _uiState.update { it.copy(settings = local) }
                ensureEmailsAdaptedForLanguage(_uiState.value.emails, local.languageCode)
            }
        }
        startClockTicker()
    }

    private fun startClockTicker() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val current = _uiState.value
                var newlyTriggered: CalendarAlarmEvent? = null
                if (current.session !is SessionMode.SignedOut && current.ringingEvent == null) {
                    newlyTriggered = current.calendarEvents.firstOrNull { evt ->
                        evt.alarmEnabled &&
                            evt.id !in current.triggeredEventIds &&
                            evt.eventTimeMillis in (now - 10_000L)..now
                    }
                }
                _uiState.update { state ->
                    if (newlyTriggered != null) {
                        alarmAudio.startLoopingAlarm(
                            ringtoneId = state.settings.alarmRingtoneId,
                            volume = state.settings.alarmVolume
                        )
                        state.copy(
                            nowMillis = now,
                            ringingEvent = newlyTriggered,
                            triggeredEventIds = state.triggeredEventIds + newlyTriggered.id
                        )
                    } else {
                        state.copy(nowMillis = now)
                    }
                }
                delay(1000L)
            }
        }
    }

    fun onGoogleAuthenticated() {
        val user = Firebase.auth.currentUser ?: return
        val role = _uiState.value.settings.role
        val displayName = user.displayName ?: user.email?.substringBefore("@") ?: "AOU Member"
        val email = user.email.orEmpty()
        _uiState.update {
            it.copy(
                session = SessionMode.GoogleUser(user.uid, displayName, email),
                emails = SampleAcademicData.sampleEmails(role),
                selectedEmailId = null,
                statusBanner = null
            )
        }
        viewModelScope.launch {
            firestoreRepo.ensureUserProfile(
                displayName = displayName,
                email = email,
                role = role,
                branch = _uiState.value.settings.aouBranch,
                majorId = _uiState.value.settings.selectedMajorId,
                languageCode = _uiState.value.settings.languageCode
            )
        }
        startFirestoreListeners(user.uid)
    }

    fun startDemoSession(role: UserRole) {
        viewModelScope.launch { preferencesRepo.setRole(role) }
        val demoName = if (role == UserRole.INSTRUCTOR) "Dr. Tariq Al-Mansoor" else "Sohaila Ahmad"
        val demoEmail = if (role == UserRole.INSTRUCTOR) "t.almansoor@aou.edu.kw" else "2341098@aou.edu.kw"
        _uiState.update {
            it.copy(
                session = SessionMode.DemoUser(demoName, demoEmail),
                settings = it.settings.copy(role = role),
                emails = SampleAcademicData.sampleEmails(role),
                calendarEvents = SampleAcademicData.defaultCalendarEvents(role),
                facultyMaterials = SampleAcademicData.defaultFacultyMaterials,
                chatMessages = SampleAcademicData.defaultChatMessages,
                manualNotes = emptyList(),
                selectedEmailId = null,
                statusBanner = null
            )
        }
        ensureEmailsAdaptedForLanguage(_uiState.value.emails, _uiState.value.settings.languageCode)
    }

    fun completeMicrosoftLogin(authCode: String) {
        viewModelScope.launch {
            val tokenRes = MicrosoftGraphClient.exchangeCodeForToken(authCode)
            if (tokenRes.isSuccess) {
                val role = _uiState.value.settings.role
                val name = MicrosoftGraphClient.signedInDisplayName ?: "Outlook User"
                val email = MicrosoftGraphClient.signedInEmail ?: "user@aou.edu.kw"
                _uiState.update {
                    it.copy(
                        session = SessionMode.MicrosoftUser(name, email),
                        calendarEvents = SampleAcademicData.defaultCalendarEvents(role),
                        statusBanner = "Connected to Microsoft Outlook ($email)"
                    )
                }
                refreshMicrosoftEmails()
            } else {
                _uiState.update {
                    it.copy(statusBanner = tokenRes.exceptionOrNull()?.localizedMessage ?: "Microsoft sign-in failed")
                }
            }
        }
    }

    fun refreshMicrosoftEmails() {
        viewModelScope.launch {
            val res = MicrosoftGraphClient.fetchMessages()
            if (res.isSuccess) {
                val list = res.getOrNull().orEmpty()
                if (list.isNotEmpty()) {
                    _uiState.update { it.copy(emails = list, statusBanner = "Synced ${list.size} Outlook emails") }
                    ensureEmailsAdaptedForLanguage(list, _uiState.value.settings.languageCode)
                }
            }
        }
    }

    private fun startFirestoreListeners(uid: String) {
        firestoreListenersJob?.cancel()
        firestoreListenersJob = viewModelScope.launch {
            launch {
                firestoreRepo.observeUserProfile(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { doc ->
                        if (doc != null) {
                            val role = UserRole.fromKey(doc.role)
                            preferencesRepo.setRole(role)
                            preferencesRepo.setBranch(doc.aouBranch.ifBlank { "Riyadh (الرياض) — Saudi Arabia" })
                            preferencesRepo.setSelectedMajor(doc.majorId.ifBlank { "cs" })
                            preferencesRepo.setCompanionGender(doc.companionGender.ifBlank { "girl" })
                            preferencesRepo.setFontScale(FontScaleOption.fromKey(doc.fontScale))
                            preferencesRepo.setDarkMode(doc.darkMode)
                            preferencesRepo.setLanguage(doc.languageCode.ifBlank { "en" })
                        }
                    }
            }
            launch {
                firestoreRepo.observeCourseProgress(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { map -> _uiState.update { it.copy(courseStatuses = map) } }
            }
            launch {
                firestoreRepo.observeCalendarEvents(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { list ->
                        if (list.isEmpty()) {
                            // Seed initial academic events once for new cloud user
                            SampleAcademicData.defaultCalendarEvents(_uiState.value.settings.role).forEach { seed ->
                                firestoreRepo.saveCalendarEvent(seed.copy(id = ""))
                            }
                        } else {
                            _uiState.update { it.copy(calendarEvents = list) }
                        }
                    }
            }
            launch {
                firestoreRepo.observeManualNotes(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { list -> _uiState.update { it.copy(manualNotes = list) } }
            }
            launch {
                firestoreRepo.observePublishedMaterials(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { list ->
                        if (list.isNotEmpty()) {
                            _uiState.update { it.copy(facultyMaterials = list) }
                        }
                    }
            }
            launch {
                firestoreRepo.observeGeneralChat(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { genList ->
                        _uiState.update { state ->
                            val directOnly = state.chatMessages.filter { it.channel == "direct" }
                            state.copy(chatMessages = directOnly + genList)
                        }
                    }
            }
            launch {
                firestoreRepo.observeDirectChat(uid)
                    .catch { e -> if (e is CancellationException) throw e }
                    .collect { dirList ->
                        _uiState.update { state ->
                            val genOnly = state.chatMessages.filter { it.channel == "general" }
                            state.copy(chatMessages = dirList + genOnly)
                        }
                    }
            }
        }
    }

    fun signOutSession() {
        firestoreListenersJob?.cancel()
        firestoreListenersJob = null
        pomodoroJob?.cancel()
        alarmAudio.stopAlarm()
        voiceRecorder.stopPlayback()
        MicrosoftGraphClient.clearSession()
        _uiState.update {
            it.copy(
                session = SessionMode.SignedOut,
                selectedEmailId = null,
                pomodoroRunning = false,
                ringingEvent = null,
                statusBanner = null
            )
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun setRole(role: UserRole) {
        viewModelScope.launch {
            preferencesRepo.setRole(role)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("role" to role.key))
            }
        }
        _uiState.update {
            it.copy(
                settings = it.settings.copy(role = role),
                emails = SampleAcademicData.sampleEmails(role),
                calendarEvents = if (it.session is SessionMode.GoogleUser) it.calendarEvents else SampleAcademicData.defaultCalendarEvents(role),
                selectedEmailId = null,
                emailSummaryState = AiUiState.Idle,
                smartReplyDraft = ""
            )
        }
        ensureEmailsAdaptedForLanguage(_uiState.value.emails, _uiState.value.settings.languageCode)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setDarkMode(enabled)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("darkMode" to enabled))
            }
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            preferencesRepo.setLanguage(code)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("languageCode" to code))
            }
        }
        ensureEmailsAdaptedForLanguage(_uiState.value.emails, code)
    }

    private fun ensureEmailsAdaptedForLanguage(emails: List<AcademicEmail>, langCode: String) {
        if (langCode == "en") return
        val targetLang = LocalizationData.find(langCode)
        // Synchronous seed for Arabic sample emails (zero flicker)
        if (langCode == "ar") {
            val immediate = mutableMapOf<String, Pair<String, String>>()
            emails.forEach { email ->
                val key = "${email.id}_ar"
                if (key !in _uiState.value.translatedEmails) {
                    SampleAcademicData.arabicEmailTranslations[email.id]?.let { pair ->
                        immediate[key] = pair
                    }
                }
            }
            if (immediate.isNotEmpty()) {
                _uiState.update { it.copy(translatedEmails = it.translatedEmails + immediate) }
            }
        }
        // Async translation for remaining/custom emails or any of the 20 languages
        viewModelScope.launch {
            emails.forEach { email ->
                val key = "${email.id}_$langCode"
                if (key !in _uiState.value.translatedEmails) {
                    val pair = aiRepo.translateEmail(email, targetLang)
                    _uiState.update { state ->
                        state.copy(translatedEmails = state.translatedEmails + (key to pair))
                    }
                }
            }
        }
    }

    fun setFontScale(option: FontScaleOption) {
        viewModelScope.launch {
            preferencesRepo.setFontScale(option)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("fontScale" to option.key))
            }
        }
    }

    fun setBranch(branch: String) {
        viewModelScope.launch {
            preferencesRepo.setBranch(branch)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("aouBranch" to branch))
            }
        }
    }

    fun setSelectedMajor(majorId: String) {
        viewModelScope.launch {
            preferencesRepo.setSelectedMajor(majorId)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("majorId" to majorId))
            }
        }
    }

    fun setCompanionGender(gender: String) {
        viewModelScope.launch {
            preferencesRepo.setCompanionGender(gender)
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.updateUserSettings(mapOf("companionGender" to gender))
            }
        }
    }

    fun setProfilePhotoUri(uri: String) {
        viewModelScope.launch { preferencesRepo.setProfilePhotoUri(uri) }
    }

    // --- Inbox & AI Email Reader ---
    fun setPriorityFilter(priority: EmailPriority?) {
        _uiState.update { it.copy(priorityFilter = priority) }
    }

    fun toggleSortByPriority() {
        _uiState.update { it.copy(sortByPriority = !it.sortByPriority) }
    }

    fun openEmail(emailId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedEmailId = emailId,
                emailSummaryState = AiUiState.Idle,
                smartReplyDraft = "",
                emails = if (emailId == null) state.emails else state.emails.map {
                    if (it.id == emailId) it.copy(unread = false, viewed = true) else it
                }
            )
        }
    }

    fun summarizeSelectedEmail(email: AcademicEmail) {
        viewModelScope.launch {
            _uiState.update { it.copy(emailSummaryState = AiUiState.Loading) }
            val lang = _uiState.value.settings.languageCode
            val summary = aiRepo.summarizeEmail(email, lang)
            _uiState.update { it.copy(emailSummaryState = AiUiState.Success(summary)) }
        }
    }

    fun generateSmartReply(email: AcademicEmail, tone: ReplyTone) {
        viewModelScope.launch {
            _uiState.update { it.copy(smartReplyLoading = true, selectedReplyTone = tone) }
            val cfg = _uiState.value.settings
            val sender = when (val s = _uiState.value.session) {
                is SessionMode.GoogleUser -> s.displayName
                is SessionMode.MicrosoftUser -> s.displayName
                is SessionMode.DemoUser -> s.displayName
                SessionMode.SignedOut -> "AOU Member"
            }
            val draft = aiRepo.generateSmartReply(
                email = email,
                tone = tone,
                role = cfg.role,
                senderName = sender,
                languageCode = cfg.languageCode
            )
            _uiState.update { it.copy(smartReplyDraft = draft, smartReplyLoading = false) }
        }
    }

    fun updateSmartReplyDraft(text: String) {
        _uiState.update { it.copy(smartReplyDraft = text) }
    }

    fun sendEmailReply(email: AcademicEmail, replyText: String) {
        if (replyText.isBlank()) return
        val cleanReply = replyText.trim()
        viewModelScope.launch {
            if (email.fromMicrosoftGraph && MicrosoftGraphClient.accessToken != null) {
                val res = MicrosoftGraphClient.replyToMessage(email.id, cleanReply)
                _uiState.update { state ->
                    state.copy(
                        emails = if (res.isSuccess) state.emails.map {
                            if (it.id == email.id) it.copy(replied = true, unread = false, viewed = true) else it
                        } else state.emails,
                        sentEmailReplies = if (res.isSuccess) state.sentEmailReplies + (email.id to cleanReply) else state.sentEmailReplies,
                        smartReplyDraft = if (res.isSuccess) "" else state.smartReplyDraft,
                        statusBanner = if (res.isSuccess) "Reply sent via Microsoft Outlook to ${email.senderName}"
                        else "Reply error: ${res.exceptionOrNull()?.localizedMessage}"
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        emails = state.emails.map {
                            if (it.id == email.id) it.copy(replied = true, unread = false, viewed = true) else it
                        },
                        sentEmailReplies = state.sentEmailReplies + (email.id to cleanReply),
                        smartReplyDraft = "",
                        statusBanner = "Reply sent to ${email.senderName} (${email.courseTag})"
                    )
                }
            }
        }
    }

    fun composeAndSendEmail(toEmail: String, subject: String, bodyText: String, priority: EmailPriority) {
        if (toEmail.isBlank() || subject.isBlank() || bodyText.isBlank()) return
        viewModelScope.launch {
            if (MicrosoftGraphClient.accessToken != null) {
                val res = MicrosoftGraphClient.sendMail(toEmail, subject, bodyText)
                _uiState.update {
                    it.copy(
                        statusBanner = if (res.isSuccess) "Email sent to $toEmail" else "Send failed: ${res.exceptionOrNull()?.localizedMessage}"
                    )
                }
            } else {
                val sent = AcademicEmail(
                    id = "sent_${UUID.randomUUID()}",
                    senderName = "You (Sent)",
                    senderEmail = "you@aou.edu.kw",
                    recipientEmail = toEmail,
                    subject = subject,
                    preview = bodyText.take(70),
                    fullBody = bodyText,
                    receivedAtLabel = "Just now",
                    timestampMillis = System.currentTimeMillis(),
                    priority = priority,
                    courseTag = "OUTBOX",
                    unread = false
                )
                _uiState.update {
                    it.copy(
                        emails = listOf(sent) + it.emails,
                        statusBanner = "Email delivered to $toEmail"
                    )
                }
                ensureEmailsAdaptedForLanguage(listOf(sent), _uiState.value.settings.languageCode)
            }
        }
    }

    fun extractDatesToCalendar(email: AcademicEmail) {
        viewModelScope.launch {
            val extracted = aiRepo.extractDatesFromEmail(email)
            val now = System.currentTimeMillis()
            extracted.forEachIndexed { idx, item ->
                val targetTime = now + ((idx + 1) * 36 * 3_600_000L)
                addCalendarEvent(
                    title = "${item.title} (${item.dateLabel})",
                    courseCode = item.courseCode,
                    category = item.category,
                    eventTimeMillis = targetTime,
                    alarmEnabled = true,
                    source = "ai_email"
                )
            }
            _uiState.update {
                it.copy(statusBanner = "Added ${extracted.size} date(s) from email to Calendar & Smart Alarm")
            }
        }
    }

    // --- Instructor Unified Reply ---
    fun sendUnifiedReply(cluster: RepeatedQuestionCluster, replyText: String) {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    answeredQuestionIds = state.answeredQuestionIds + cluster.id,
                    answeredQuestionReplies = state.answeredQuestionReplies + (cluster.id to replyText.trim()),
                    emails = state.emails.map { email ->
                        if (email.courseTag.equals(cluster.courseCode, ignoreCase = true) ||
                            cluster.studentEmails.any { s -> email.senderEmail.equals(s, ignoreCase = true) }
                        ) {
                            email.copy(replied = true, unread = false, viewed = true)
                        } else {
                            email
                        }
                    },
                    statusBanner = "Unified reply sent to ${cluster.askCount} students for ${cluster.courseCode}"
                )
            }
            sendChatMessage(
                channel = "direct",
                courseTag = cluster.courseCode,
                text = "[Unified Faculty Reply — ${cluster.topic}]\n$replyText",
                imageUri = null,
                context = null
            )
        }
    }

    // --- Calendar & Continuous Smart Alarm ---
    fun addCalendarEvent(
        title: String,
        courseCode: String,
        category: String,
        eventTimeMillis: Long,
        alarmEnabled: Boolean,
        source: String = "manual"
    ) {
        if (title.isBlank()) return
        val newEvent = CalendarAlarmEvent(
            id = "evt_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = (_uiState.value.session as? SessionMode.GoogleUser)?.uid ?: "demo",
            title = title.trim(),
            courseCode = courseCode.trim().uppercase().ifBlank { "AOU" },
            category = category,
            eventTimeMillis = eventTimeMillis,
            alarmEnabled = alarmEnabled,
            ringtoneId = _uiState.value.settings.alarmRingtoneId,
            source = source
        )
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.saveCalendarEvent(newEvent.copy(id = ""))
            } else {
                _uiState.update {
                    it.copy(calendarEvents = (it.calendarEvents + newEvent).sortedBy { e -> e.eventTimeMillis })
                }
            }
        }
    }

    fun toggleEventAlarm(event: CalendarAlarmEvent) {
        val next = !event.alarmEnabled
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.toggleEventAlarm(event.id, next)
            } else {
                _uiState.update { state ->
                    state.copy(
                        calendarEvents = state.calendarEvents.map {
                            if (it.id == event.id) it.copy(alarmEnabled = next) else it
                        }
                    )
                }
            }
        }
    }

    fun deleteCalendarEvent(eventId: String) {
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.deleteCalendarEvent(eventId)
            } else {
                _uiState.update { state ->
                    state.copy(calendarEvents = state.calendarEvents.filterNot { it.id == eventId })
                }
            }
        }
    }

    fun updateAlarmSoundSettings(ringtoneId: String, volume: Float) {
        viewModelScope.launch { preferencesRepo.setAlarmSettings(ringtoneId, volume) }
    }

    fun previewRingtone(ringtoneId: String, volume: Float) {
        alarmAudio.previewTone(ringtoneId, volume)
    }

    fun testContinuousAlarm(event: CalendarAlarmEvent) {
        val settings = _uiState.value.settings
        alarmAudio.startLoopingAlarm(settings.alarmRingtoneId, settings.alarmVolume)
        _uiState.update { it.copy(ringingEvent = event) }
    }

    fun dismissRingingAlarm() {
        alarmAudio.stopAlarm()
        _uiState.update { it.copy(ringingEvent = null) }
    }

    fun snoozeRingingAlarm5Min() {
        val ringing = _uiState.value.ringingEvent ?: return
        alarmAudio.stopAlarm()
        val snoozedTime = System.currentTimeMillis() + 5 * 60_000L
        _uiState.update { state ->
            state.copy(
                ringingEvent = null,
                triggeredEventIds = state.triggeredEventIds - ringing.id,
                calendarEvents = state.calendarEvents.map {
                    if (it.id == ringing.id) it.copy(eventTimeMillis = snoozedTime, alarmEnabled = true) else it
                }.sortedBy { it.eventTimeMillis },
                statusBanner = "Alarm snoozed for 5 minutes"
            )
        }
    }

    // --- Pomodoro Timer & 30-Minute Milestones ---
    fun selectPomodoroDuration(minutes: Int) {
        if (_uiState.value.pomodoroRunning) return
        _uiState.update {
            it.copy(
                pomodoroDurationMinutes = minutes,
                pomodoroIsBreak = false,
                pomodoroSecondsRemaining = minutes * 60
            )
        }
    }

    fun startPomodoroFocus() {
        if (_uiState.value.pomodoroRunning) return
        _uiState.update { it.copy(pomodoroRunning = true) }
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val curr = _uiState.value
                if (!curr.pomodoroRunning) break
                if (curr.pomodoroSecondsRemaining > 1) {
                    _uiState.update { it.copy(pomodoroSecondsRemaining = it.pomodoroSecondsRemaining - 1) }
                } else {
                    if (!curr.pomodoroIsBreak) {
                        val earned = curr.pomodoroDurationMinutes
                        val prevTotal = curr.settings.totalPomodoroMinutes
                        val newTotal = prevTotal + earned
                        preferencesRepo.addPomodoroMinutes(earned)
                        val crossedMilestone = (newTotal / 30) > (prevTotal / 30) && newTotal >= 30
                        alarmAudio.previewTone(curr.settings.alarmRingtoneId, curr.settings.alarmVolume)
                        _uiState.update {
                            it.copy(
                                pomodoroRunning = false,
                                pomodoroIsBreak = true,
                                pomodoroSecondsRemaining = 5 * 60,
                                milestoneAwardMinutes = if (crossedMilestone) (newTotal / 30) * 30 else null,
                                statusBanner = "Focus session complete! +$earned min logged."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                pomodoroRunning = false,
                                pomodoroIsBreak = false,
                                pomodoroSecondsRemaining = it.pomodoroDurationMinutes * 60,
                                statusBanner = "Break complete! Ready for next study block."
                            )
                        }
                    }
                    break
                }
            }
        }
    }

    fun startBreakTimer() {
        if (_uiState.value.pomodoroRunning) return
        _uiState.update {
            it.copy(
                pomodoroIsBreak = true,
                pomodoroSecondsRemaining = 5 * 60,
                pomodoroRunning = true
            )
        }
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val curr = _uiState.value
                if (!curr.pomodoroRunning) break
                if (curr.pomodoroSecondsRemaining > 1) {
                    _uiState.update { it.copy(pomodoroSecondsRemaining = it.pomodoroSecondsRemaining - 1) }
                } else {
                    _uiState.update {
                        it.copy(
                            pomodoroRunning = false,
                            pomodoroIsBreak = false,
                            pomodoroSecondsRemaining = it.pomodoroDurationMinutes * 60
                        )
                    }
                    break
                }
            }
        }
    }

    fun exitPomodoroFocus() {
        pomodoroJob?.cancel()
        pomodoroJob = null
        _uiState.update { state ->
            state.copy(
                pomodoroRunning = false,
                pomodoroIsBreak = false,
                pomodoroSecondsRemaining = state.pomodoroDurationMinutes * 60
            )
        }
    }

    fun dismissMilestonePopup() {
        _uiState.update { it.copy(milestoneAwardMinutes = null) }
    }

    // --- Roadmaps ---
    fun setCourseStatus(majorId: String, courseCode: String, status: CourseStatus) {
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.setCourseStatus(majorId, courseCode, status)
            } else {
                _uiState.update {
                    it.copy(courseStatuses = it.courseStatuses + (courseCode to status))
                }
            }
        }
    }

    // --- Dual Chat ---
    fun sendChatMessage(
        channel: String,
        courseTag: String,
        text: String,
        imageUri: Uri?,
        context: Context?
    ) {
        if (text.isBlank() && imageUri == null) return
        viewModelScope.launch {
            val base64Image = if (imageUri != null && context != null) {
                compressImageToBase64(context, imageUri)
            } else null

            val session = _uiState.value.session
            val role = _uiState.value.settings.role
            val senderName = when (session) {
                is SessionMode.GoogleUser -> session.displayName
                is SessionMode.MicrosoftUser -> session.displayName
                is SessionMode.DemoUser -> session.displayName
                SessionMode.SignedOut -> "AOU Member"
            }

            if (session is SessionMode.GoogleUser) {
                firestoreRepo.sendChatMessage(
                    senderName = senderName,
                    senderRole = role,
                    channel = channel,
                    peerUid = if (channel == "direct") "aou_faculty_support" else null,
                    courseTag = courseTag.ifBlank { "AOU" },
                    text = text.ifBlank { "[Image Attachment]" },
                    attachmentBase64 = base64Image
                )
            } else {
                val msg = ChatMessageItem(
                    id = "msg_${UUID.randomUUID()}",
                    senderId = if (role == UserRole.INSTRUCTOR) "demo_instructor" else "demo_student",
                    senderName = senderName,
                    senderRole = role.key,
                    channel = channel,
                    participants = if (channel == "direct") listOf("demo_student", "demo_instructor") else emptyList(),
                    courseTag = courseTag.ifBlank { "AOU" },
                    text = text.ifBlank { "[Image Attachment]" },
                    attachmentBase64 = base64Image,
                    createdAt = Timestamp.now()
                )
                _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
            }
        }
    }

    private suspend fun compressImageToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val original = BitmapFactory.decodeStream(stream)
            stream.close()
            if (original == null) return@withContext null
            val maxDim = 640
            val scale = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1f)
            val resized = Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt().coerceAtLeast(1),
                (original.height * scale).toInt().coerceAtLeast(1),
                true
            )
            val out = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    // --- Faculty Materials & Student Manual Notes ---
    fun publishFacultyMaterial(
        courseCode: String,
        title: String,
        description: String,
        resourceUrl: String,
        materialType: String
    ) {
        if (courseCode.isBlank() || title.isBlank()) return
        val session = _uiState.value.session
        val authorName = when (session) {
            is SessionMode.GoogleUser -> session.displayName
            is SessionMode.MicrosoftUser -> session.displayName
            is SessionMode.DemoUser -> session.displayName
            SessionMode.SignedOut -> "AOU Faculty"
        }
        viewModelScope.launch {
            if (session is SessionMode.GoogleUser) {
                firestoreRepo.publishFacultyMaterial(authorName, courseCode, title, description, resourceUrl, materialType)
            } else {
                val item = FacultyMaterialItem(
                    id = "mat_${UUID.randomUUID()}",
                    authorId = "demo_instructor",
                    authorName = authorName,
                    courseCode = courseCode.trim().uppercase(),
                    title = title.trim(),
                    description = description.trim(),
                    resourceUrl = resourceUrl.trim(),
                    materialType = materialType,
                    published = true
                )
                _uiState.update {
                    it.copy(
                        facultyMaterials = listOf(item) + it.facultyMaterials,
                        statusBanner = "Published ${item.courseCode} material: ${item.title}"
                    )
                }
            }
        }
    }

    fun deleteFacultyMaterial(materialId: String) {
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.deleteFacultyMaterial(materialId)
            } else {
                _uiState.update { state ->
                    state.copy(facultyMaterials = state.facultyMaterials.filterNot { it.id == materialId })
                }
            }
        }
    }

    fun addManualNote(title: String, content: String, linkUrl: String, courseTag: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.addManualNote(title, content, linkUrl, courseTag)
            } else {
                val note = ManualNoteItem(
                    id = "note_${UUID.randomUUID()}",
                    userId = "demo",
                    title = title.trim(),
                    content = content.trim(),
                    linkUrl = linkUrl.trim(),
                    courseTag = courseTag.trim().uppercase()
                )
                _uiState.update { it.copy(manualNotes = listOf(note) + it.manualNotes) }
            }
        }
    }

    fun deleteManualNote(noteId: String) {
        viewModelScope.launch {
            if (_uiState.value.session is SessionMode.GoogleUser) {
                firestoreRepo.deleteManualNote(noteId)
            } else {
                _uiState.update { state ->
                    state.copy(manualNotes = state.manualNotes.filterNot { it.id == noteId })
                }
            }
        }
    }

    // --- Study Assistant & Freshmen Q&A ---
    fun askStudyAssistant(question: String) {
        if (question.isBlank() || _uiState.value.studyStreaming) return
        val majorName = AouRoadmapsData.allMajors
            .firstOrNull { it.id == _uiState.value.settings.selectedMajorId }
            ?.nameEn ?: "Computer Science"
        val userMsg = StudyMessage(UUID.randomUUID().toString(), true, question.trim())
        val botId = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                studyMessages = it.studyMessages + userMsg + StudyMessage(botId, false, "Thinking..."),
                studyStreaming = true
            )
        }
        viewModelScope.launch {
            val lang = _uiState.value.settings.languageCode
            aiRepo.streamStudyAssistant(question.trim(), majorName, lang)
                .collect { partial ->
                    _uiState.update { state ->
                        state.copy(
                            studyMessages = state.studyMessages.map {
                                if (it.id == botId) it.copy(text = partial) else it
                            }
                        )
                    }
                }
            _uiState.update { it.copy(studyStreaming = false) }
        }
    }

    fun askFreshmanQuestionAi(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(freshmanAiState = AiUiState.Loading) }
            val lang = _uiState.value.settings.languageCode
            val sb = StringBuilder()
            aiRepo.streamStudyAssistant(
                question = "AOU Freshman question: $question",
                majorName = "Arab Open University General Orientation",
                languageCode = lang
            ).collect { chunk ->
                sb.clear()
                sb.append(chunk)
            }
            _uiState.update { it.copy(freshmanAiState = AiUiState.Success(sb.toString())) }
        }
    }

    // --- Voice Notes ---
    fun startVoiceRecording() {
        val file = voiceRecorder.startRecording()
        if (file != null) {
            _uiState.update { it.copy(recordingVoice = true) }
        } else {
            _uiState.update { it.copy(statusBanner = "Unable to start microphone recording") }
        }
    }

    fun stopVoiceRecording(title: String) {
        val result = voiceRecorder.stopRecording()
        _uiState.update { it.copy(recordingVoice = false) }
        if (result != null) {
            val (file, sec) = result
            val item = VoiceNoteItem(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Study Voice Note" },
                filePath = file.absolutePath,
                durationSec = sec,
                createdAtMillis = System.currentTimeMillis()
            )
            _uiState.update { it.copy(voiceNotes = listOf(item) + it.voiceNotes) }
        }
    }

    fun togglePlayVoiceNote(note: VoiceNoteItem) {
        if (_uiState.value.playingVoiceNoteId == note.id) {
            voiceRecorder.stopPlayback()
            _uiState.update { it.copy(playingVoiceNoteId = null) }
        } else {
            _uiState.update { it.copy(playingVoiceNoteId = note.id) }
            voiceRecorder.play(note.filePath) {
                _uiState.update { it.copy(playingVoiceNoteId = null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        pomodoroJob?.cancel()
        firestoreListenersJob?.cancel()
        alarmAudio.stopAlarm()
        voiceRecorder.stopPlayback()
    }
}
