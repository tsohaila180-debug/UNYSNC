package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.GoogleAuthHelper
import com.example.data.AlarmAudioController
import com.example.data.FirebaseAiRepository
import com.example.data.FirestoreRepository
import com.example.data.LocalizationData
import com.example.data.PreferencesRepository
import com.example.data.UiStringProvider
import com.example.data.VoiceNoteRecorder
import com.example.data.configureAppCheck
import com.example.model.UserRole
import com.example.ui.components.FullScreenAlarmOverlay
import com.example.ui.components.UniSyncAppIconBadge
import com.example.ui.screens.CalendarAndAlarmScreen
import com.example.ui.screens.DualChatScreen
import com.example.ui.screens.FreshmenQaScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.InstructorHomeScreen
import com.example.ui.screens.InstructorMaterialsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.RepliedAndReviewedScreen
import com.example.ui.screens.RoadmapsScreen
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import com.example.ui.screens.StudentFacultyMaterialsReadScreen
import com.example.ui.screens.StudentHomeScreen
import com.example.ui.screens.StudentManualNotesScreen
import com.example.ui.screens.StudentSubTool
import com.example.ui.screens.StudentToolsHubScreen
import com.example.ui.screens.StudyAssistantScreen
import com.example.ui.screens.VoiceNotesScreen
import com.example.ui.theme.UniSyncTheme
import com.example.viewmodel.SessionMode
import com.example.viewmodel.UniSyncUiState
import com.example.viewmodel.UniSyncViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

private enum class StudentTab {
    HOME, INBOX, POMODORO, CHAT, ROADMAPS, TOOLS
}

private enum class InstructorTab {
    HOME, INBOX, CALENDAR, MATERIALS, CHAT, SETTINGS
}

class MainActivity : ComponentActivity() {

    private var pendingMicrosoftCode = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory Firebase App Check wiring BEFORE Compose UI or ViewModels
        configureAppCheck(this, intent)
        handleMicrosoftRedirect(intent)
        enableEdgeToEdge()

        setContent {
            val viewModel: UniSyncViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        UniSyncViewModel(
                            preferencesRepo = PreferencesRepository(app),
                            firestoreRepo = FirestoreRepository(app),
                            aiRepo = FirebaseAiRepository(),
                            alarmAudio = AlarmAudioController(app),
                            voiceRecorder = VoiceNoteRecorder(app)
                        )
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val settings = uiState.settings
            val isRtl = LocalizationData.isRtl(settings.languageCode)
            val strings = UiStringProvider.get(settings.languageCode)

            val authCode = pendingMicrosoftCode.value
            LaunchedEffect(authCode) {
                if (!authCode.isNullOrBlank()) {
                    viewModel.completeMicrosoftLogin(authCode)
                    pendingMicrosoftCode.value = null
                }
            }

            // Observe Firebase Auth session transitions cleanly
            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    if (auth.currentUser != null && uiState.session !is SessionMode.GoogleUser) {
                        viewModel.onGoogleAuthenticated()
                    }
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose { Firebase.auth.removeAuthStateListener(listener) }
            }

            UniSyncTheme(
                role = settings.role,
                darkMode = settings.darkMode,
                fontScale = settings.fontScale,
                isRtl = isRtl
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val session = uiState.session) {
                        SessionMode.SignedOut -> {
                            LoginScreen(
                                selectedRole = settings.role,
                                darkMode = settings.darkMode,
                                languageCode = settings.languageCode,
                                strings = strings,
                                onSelectRole = viewModel::setRole,
                                onToggleDarkMode = viewModel::setDarkMode,
                                onSelectLanguage = viewModel::setLanguage,
                                onGoogleSuccess = viewModel::onGoogleAuthenticated,
                                onMicrosoftCodeSubmitted = viewModel::completeMicrosoftLogin,
                                onDemoLogin = viewModel::startDemoSession
                            )
                        }
                        else -> {
                            val sessionKey = when (session) {
                                is SessionMode.GoogleUser -> session.uid
                                is SessionMode.MicrosoftUser -> "ms_${session.email}"
                                is SessionMode.DemoUser -> "demo_${settings.role.key}"
                                SessionMode.SignedOut -> "out"
                            }
                            key(sessionKey) {
                                UniSyncWorkspace(
                                    uiState = uiState,
                                    isRtl = isRtl,
                                    strings = strings,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }

                    // Continuous Looping Smart Alarm Overlay
                    uiState.ringingEvent?.let { alarmEvent ->
                        FullScreenAlarmOverlay(
                            event = alarmEvent,
                            onSnooze5Min = viewModel::snoozeRingingAlarm5Min,
                            onDismiss = viewModel::dismissRingingAlarm
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleMicrosoftRedirect(intent)
    }

    private fun handleMicrosoftRedirect(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "unisync" && data.host == "auth" && data.path?.startsWith("/microsoft") == true) {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                pendingMicrosoftCode.value = code
            }
        }
    }
}

private data class NavEntry(
    val label: String,
    val icon: ImageVector,
    val tag: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniSyncWorkspace(
    uiState: UniSyncUiState,
    isRtl: Boolean,
    strings: com.example.data.UiStrings,
    viewModel: UniSyncViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val settings = uiState.settings
    val snackbarHostState = remember { SnackbarHostState() }

    var studentTab by remember { mutableStateOf(StudentTab.HOME) }
    var studentSubTool by remember { mutableStateOf(StudentSubTool.HUB) }
    var instructorTab by remember { mutableStateOf(InstructorTab.HOME) }
    var showRepliedReviewed by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val isFocusLocked = settings.role == UserRole.STUDENT &&
        studentTab == StudentTab.POMODORO &&
        uiState.pomodoroRunning &&
        !uiState.pomodoroIsBreak

    LaunchedEffect(uiState.statusBanner) {
        val msg = uiState.statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val nextEvent = remember(uiState.calendarEvents, uiState.nowMillis) {
        uiState.calendarEvents
            .filter { it.eventTimeMillis >= uiState.nowMillis - 60_000L }
            .minByOrNull { it.eventTimeMillis }
            ?: uiState.calendarEvents.firstOrNull()
    }

    val (displayName, email) = when (val s = uiState.session) {
        is SessionMode.GoogleUser -> s.displayName to s.email
        is SessionMode.MicrosoftUser -> s.displayName to s.email
        is SessionMode.DemoUser -> s.displayName to s.email
        SessionMode.SignedOut -> "AOU User" to ""
    }

    val historyCount = remember(uiState.answeredQuestionIds, uiState.emails, settings.role) {
        val emailHistoryCount = uiState.emails.count { it.replied || it.viewed }
        if (settings.role == UserRole.INSTRUCTOR) {
            uiState.answeredQuestionIds.size + emailHistoryCount
        } else {
            emailHistoryCount
        }
    }

    val navItems = if (settings.role == UserRole.STUDENT) {
        listOf(
            NavEntry(strings.home, Icons.Filled.Home, "nav_home", !showRepliedReviewed && studentTab == StudentTab.HOME) {
                showRepliedReviewed = false
                studentTab = StudentTab.HOME
            },
            NavEntry(strings.inbox, Icons.Filled.Mail, "nav_inbox", !showRepliedReviewed && studentTab == StudentTab.INBOX) {
                showRepliedReviewed = false
                studentTab = StudentTab.INBOX
            },
            NavEntry(strings.pomodoro, Icons.Filled.Timer, "nav_pomodoro", !showRepliedReviewed && studentTab == StudentTab.POMODORO) {
                showRepliedReviewed = false
                studentTab = StudentTab.POMODORO
            },
            NavEntry(strings.chat, Icons.Filled.Chat, "nav_chat", !showRepliedReviewed && studentTab == StudentTab.CHAT) {
                showRepliedReviewed = false
                studentTab = StudentTab.CHAT
            },
            NavEntry(strings.roadmaps, Icons.Filled.Map, "nav_roadmaps", !showRepliedReviewed && studentTab == StudentTab.ROADMAPS) {
                showRepliedReviewed = false
                studentTab = StudentTab.ROADMAPS
            },
            NavEntry(strings.tools, Icons.Filled.Build, "nav_tools", !showRepliedReviewed && studentTab == StudentTab.TOOLS) {
                showRepliedReviewed = false
                studentTab = StudentTab.TOOLS
                studentSubTool = StudentSubTool.HUB
            }
        )
    } else {
        listOf(
            NavEntry(strings.home, Icons.Filled.Home, "nav_home", !showRepliedReviewed && instructorTab == InstructorTab.HOME) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.HOME
            },
            NavEntry(strings.inbox, Icons.Filled.Mail, "nav_inbox", !showRepliedReviewed && instructorTab == InstructorTab.INBOX) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.INBOX
            },
            NavEntry(strings.calendarAlarm, Icons.Filled.CalendarMonth, "nav_calendar", !showRepliedReviewed && instructorTab == InstructorTab.CALENDAR) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.CALENDAR
            },
            NavEntry(strings.materials, Icons.Filled.FolderOpen, "nav_materials", !showRepliedReviewed && instructorTab == InstructorTab.MATERIALS) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.MATERIALS
            },
            NavEntry(strings.chat, Icons.Filled.Chat, "nav_chat", !showRepliedReviewed && instructorTab == InstructorTab.CHAT) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.CHAT
            },
            NavEntry(strings.settings, Icons.Filled.Settings, "nav_settings", !showRepliedReviewed && instructorTab == InstructorTab.SETTINGS) {
                showRepliedReviewed = false
                instructorTab = InstructorTab.SETTINGS
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isFocusLocked,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UniSyncAppIconBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = "UniSync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (settings.role == UserRole.INSTRUCTOR) strings.instructorRole else strings.studentRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(6.dp))

                // Standalone Replied & Reviewed History Section
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.repliedReviewed, fontWeight = FontWeight.SemiBold)
                            if (historyCount > 0) {
                                Text(
                                    text = "$historyCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    icon = { Icon(Icons.Filled.History, contentDescription = strings.repliedReviewed) },
                    selected = showRepliedReviewed,
                    onClick = {
                        showRepliedReviewed = true
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("drawer_replied_reviewed")
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = item.selected,
                        onClick = {
                            item.onClick()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    label = { Text(strings.signOut) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = strings.signOut) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        GoogleAuthHelper.signOut(credentialManager, scope) {
                            viewModel.signOutSession()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (!isFocusLocked) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("side_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = strings.menu
                                    )
                                }
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UniSyncAppIconBadge(size = 32.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "UniSync",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.setDarkMode(!settings.darkMode) }) {
                                    Icon(
                                        imageVector = if (settings.darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                        contentDescription = strings.darkMode
                                    )
                                }
                                if (settings.role == UserRole.STUDENT) {
                                    IconButton(
                                        onClick = {
                                            showRepliedReviewed = false
                                            studentTab = StudentTab.TOOLS
                                            studentSubTool = StudentSubTool.SETTINGS
                                        }
                                    ) {
                                        Icon(Icons.Filled.Settings, contentDescription = strings.settings)
                                    }
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (!useRail && !isFocusLocked) {
                        NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    selected = item.selected,
                                    onClick = item.onClick,
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.testTag(item.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail && !isFocusLocked) {
                        NavigationRail {
                            navItems.forEach { item ->
                                NavigationRailItem(
                                    selected = item.selected,
                                    onClick = item.onClick,
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.testTag(item.tag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (showRepliedReviewed && !isFocusLocked) {
                            RepliedAndReviewedScreen(
                                role = settings.role,
                                repeatedQuestions = uiState.repeatedQuestions,
                                answeredQuestionIds = uiState.answeredQuestionIds,
                                answeredQuestionReplies = uiState.answeredQuestionReplies,
                                emails = uiState.emails,
                                sentEmailReplies = uiState.sentEmailReplies,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onOpenEmail = { emailId ->
                                    showRepliedReviewed = false
                                    if (settings.role == UserRole.INSTRUCTOR) {
                                        instructorTab = InstructorTab.INBOX
                                    } else {
                                        studentTab = StudentTab.INBOX
                                    }
                                    viewModel.openEmail(emailId)
                                },
                                onBack = { showRepliedReviewed = false }
                            )
                        } else if (settings.role == UserRole.STUDENT) {
                        when (studentTab) {
                            StudentTab.HOME -> StudentHomeScreen(
                                nextEvent = nextEvent,
                                events = uiState.calendarEvents,
                                emails = uiState.emails,
                                nowMillis = uiState.nowMillis,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onToggleAlarm = viewModel::toggleEventAlarm,
                                onOpenFullCalendar = {
                                    studentTab = StudentTab.TOOLS
                                    studentSubTool = StudentSubTool.CALENDAR
                                },
                                onOpenEmail = { emailId ->
                                    studentTab = StudentTab.INBOX
                                    viewModel.openEmail(emailId)
                                },
                                onTestRing = viewModel::testContinuousAlarm
                            )
                            StudentTab.INBOX -> InboxScreen(
                                emails = uiState.emails,
                                priorityFilter = uiState.priorityFilter,
                                sortByPriority = uiState.sortByPriority,
                                selectedEmailId = uiState.selectedEmailId,
                                summaryState = uiState.emailSummaryState,
                                smartReplyDraft = uiState.smartReplyDraft,
                                smartReplyLoading = uiState.smartReplyLoading,
                                selectedTone = uiState.selectedReplyTone,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onSelectFilter = viewModel::setPriorityFilter,
                                onToggleSort = viewModel::toggleSortByPriority,
                                onOpenEmail = viewModel::openEmail,
                                onSummarizeEmail = viewModel::summarizeSelectedEmail,
                                onGenerateSmartReply = viewModel::generateSmartReply,
                                onDraftChange = viewModel::updateSmartReplyDraft,
                                onSendReply = viewModel::sendEmailReply,
                                onExtractDates = viewModel::extractDatesToCalendar,
                                onComposeSend = viewModel::composeAndSendEmail
                            )
                            StudentTab.POMODORO -> PomodoroScreen(
                                durationMinutes = uiState.pomodoroDurationMinutes,
                                secondsRemaining = uiState.pomodoroSecondsRemaining,
                                running = uiState.pomodoroRunning,
                                isBreak = uiState.pomodoroIsBreak,
                                totalMinutesStudied = settings.totalPomodoroMinutes,
                                companionGender = settings.companionGender,
                                milestoneAwardMinutes = uiState.milestoneAwardMinutes,
                                isRtl = isRtl,
                                strings = strings,
                                onSelectDuration = viewModel::selectPomodoroDuration,
                                onStartFocus = viewModel::startPomodoroFocus,
                                onStartBreak = viewModel::startBreakTimer,
                                onExitFocus = viewModel::exitPomodoroFocus,
                                onSelectGender = viewModel::setCompanionGender,
                                onDismissMilestone = viewModel::dismissMilestonePopup
                            )
                            StudentTab.CHAT -> DualChatScreen(
                                role = UserRole.STUDENT,
                                messages = uiState.chatMessages,
                                strings = strings,
                                onSendMessage = { ch, tag, txt, uri ->
                                    viewModel.sendChatMessage(ch, tag, txt, uri, context)
                                }
                            )
                            StudentTab.ROADMAPS -> RoadmapsScreen(
                                selectedMajorId = settings.selectedMajorId,
                                courseStatuses = uiState.courseStatuses,
                                isRtl = isRtl,
                                strings = strings,
                                onSelectMajor = viewModel::setSelectedMajor,
                                onUpdateCourseStatus = viewModel::setCourseStatus
                            )
                            StudentTab.TOOLS -> StudentToolsHubScreen(
                                activeTool = studentSubTool,
                                onSelectTool = { studentSubTool = it },
                                calendarContent = {
                                    CalendarAndAlarmScreen(
                                        events = uiState.calendarEvents,
                                        emails = uiState.emails,
                                        nowMillis = uiState.nowMillis,
                                        selectedRingtoneId = settings.alarmRingtoneId,
                                        alarmVolume = settings.alarmVolume,
                                        languageCode = settings.languageCode,
                                        translatedEmails = uiState.translatedEmails,
                                        isRtl = isRtl,
                                        strings = strings,
                                        onAddEvent = viewModel::addCalendarEvent,
                                        onToggleAlarm = viewModel::toggleEventAlarm,
                                        onDeleteEvent = viewModel::deleteCalendarEvent,
                                        onUpdateSoundSettings = viewModel::updateAlarmSoundSettings,
                                        onPreviewRingtone = viewModel::previewRingtone,
                                        onTestRing = viewModel::testContinuousAlarm,
                                        onOpenEmail = { emailId ->
                                            studentTab = StudentTab.INBOX
                                            viewModel.openEmail(emailId)
                                        }
                                    )
                                },
                                studyAssistantContent = {
                                    StudyAssistantScreen(
                                        messages = uiState.studyMessages,
                                        streaming = uiState.studyStreaming,
                                        onAsk = viewModel::askStudyAssistant
                                    )
                                },
                                freshmenQaContent = {
                                    FreshmenQaScreen(
                                        aiState = uiState.freshmanAiState,
                                        onAskAi = viewModel::askFreshmanQuestionAi
                                    )
                                },
                                voiceNotesContent = {
                                    VoiceNotesScreen(
                                        notes = uiState.voiceNotes,
                                        recording = uiState.recordingVoice,
                                        playingId = uiState.playingVoiceNoteId,
                                        onStartRecord = viewModel::startVoiceRecording,
                                        onStopRecord = viewModel::stopVoiceRecording,
                                        onTogglePlay = viewModel::togglePlayVoiceNote
                                    )
                                },
                                manualNotesContent = {
                                    StudentManualNotesScreen(
                                        notes = uiState.manualNotes,
                                        onAddNote = viewModel::addManualNote,
                                        onDeleteNote = viewModel::deleteManualNote
                                    )
                                },
                                facultyMaterialsContent = {
                                    StudentFacultyMaterialsReadScreen(materials = uiState.facultyMaterials)
                                },
                                settingsContent = {
                                    SettingsScreen(
                                        displayName = displayName,
                                        email = email,
                                        settings = settings,
                                        isRtl = isRtl,
                                        strings = strings,
                                        onPhotoPicked = viewModel::setProfilePhotoUri,
                                        onToggleDarkMode = viewModel::setDarkMode,
                                        onSelectFontScale = viewModel::setFontScale,
                                        onSelectLanguage = viewModel::setLanguage,
                                        onSelectBranch = viewModel::setBranch,
                                        onSignOut = {
                                            GoogleAuthHelper.signOut(credentialManager, scope) {
                                                viewModel.signOutSession()
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    } else {
                        when (instructorTab) {
                            InstructorTab.HOME -> InstructorHomeScreen(
                                repeatedQuestions = uiState.repeatedQuestions,
                                answeredQuestionIds = uiState.answeredQuestionIds,
                                nextEvent = nextEvent,
                                nowMillis = uiState.nowMillis,
                                emails = uiState.emails,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onSendUnifiedReply = viewModel::sendUnifiedReply,
                                onOpenNextUpCalendar = { instructorTab = InstructorTab.CALENDAR },
                                onOpenEmail = { id ->
                                    instructorTab = InstructorTab.INBOX
                                    viewModel.openEmail(id)
                                },
                                onOpenFullInbox = { instructorTab = InstructorTab.INBOX }
                            )
                            InstructorTab.INBOX -> InboxScreen(
                                emails = uiState.emails,
                                priorityFilter = uiState.priorityFilter,
                                sortByPriority = uiState.sortByPriority,
                                selectedEmailId = uiState.selectedEmailId,
                                summaryState = uiState.emailSummaryState,
                                smartReplyDraft = uiState.smartReplyDraft,
                                smartReplyLoading = uiState.smartReplyLoading,
                                selectedTone = uiState.selectedReplyTone,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onSelectFilter = viewModel::setPriorityFilter,
                                onToggleSort = viewModel::toggleSortByPriority,
                                onOpenEmail = viewModel::openEmail,
                                onSummarizeEmail = viewModel::summarizeSelectedEmail,
                                onGenerateSmartReply = viewModel::generateSmartReply,
                                onDraftChange = viewModel::updateSmartReplyDraft,
                                onSendReply = viewModel::sendEmailReply,
                                onExtractDates = viewModel::extractDatesToCalendar,
                                onComposeSend = viewModel::composeAndSendEmail
                            )
                            InstructorTab.CALENDAR -> CalendarAndAlarmScreen(
                                events = uiState.calendarEvents,
                                emails = uiState.emails,
                                nowMillis = uiState.nowMillis,
                                selectedRingtoneId = settings.alarmRingtoneId,
                                alarmVolume = settings.alarmVolume,
                                languageCode = settings.languageCode,
                                translatedEmails = uiState.translatedEmails,
                                isRtl = isRtl,
                                strings = strings,
                                onAddEvent = viewModel::addCalendarEvent,
                                onToggleAlarm = viewModel::toggleEventAlarm,
                                onDeleteEvent = viewModel::deleteCalendarEvent,
                                onUpdateSoundSettings = viewModel::updateAlarmSoundSettings,
                                onPreviewRingtone = viewModel::previewRingtone,
                                onTestRing = viewModel::testContinuousAlarm,
                                onOpenEmail = { emailId ->
                                    instructorTab = InstructorTab.INBOX
                                    viewModel.openEmail(emailId)
                                }
                            )
                            InstructorTab.MATERIALS -> InstructorMaterialsScreen(
                                materials = uiState.facultyMaterials,
                                strings = strings,
                                onPublish = viewModel::publishFacultyMaterial,
                                onDelete = viewModel::deleteFacultyMaterial
                            )
                            InstructorTab.CHAT -> DualChatScreen(
                                role = UserRole.INSTRUCTOR,
                                messages = uiState.chatMessages,
                                strings = strings,
                                onSendMessage = { ch, tag, txt, uri ->
                                    viewModel.sendChatMessage(ch, tag, txt, uri, context)
                                }
                            )
                            InstructorTab.SETTINGS -> SettingsScreen(
                                displayName = displayName,
                                email = email,
                                settings = settings,
                                isRtl = isRtl,
                                strings = strings,
                                onPhotoPicked = viewModel::setProfilePhotoUri,
                                onToggleDarkMode = viewModel::setDarkMode,
                                onSelectFontScale = viewModel::setFontScale,
                                onSelectLanguage = viewModel::setLanguage,
                                onSelectBranch = viewModel::setBranch,
                                onSignOut = {
                                    GoogleAuthHelper.signOut(credentialManager, scope) {
                                        viewModel.signOutSession()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
