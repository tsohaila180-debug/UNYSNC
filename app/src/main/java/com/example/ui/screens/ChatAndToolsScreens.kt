package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.LocalizationData
import com.example.data.LocalUserSettings
import com.example.data.SampleAcademicData
import com.example.data.UiStrings
import com.example.model.AiUiState
import com.example.model.ChatMessageItem
import com.example.model.FacultyMaterialItem
import com.example.model.FontScaleOption
import com.example.model.ManualNoteItem
import com.example.model.StudyMessage
import com.example.model.UserRole
import com.example.model.VoiceNoteItem
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.PlatformsDownloadDialog
import com.example.ui.components.ProfileAvatarWithSingleCamera
import com.example.ui.theme.LocalUniSyncColors

@Composable
fun DualChatScreen(
    role: UserRole,
    messages: List<ChatMessageItem>,
    strings: UiStrings,
    onSendMessage: (String, String, String, Uri?) -> Unit
) {
    val ext = LocalUniSyncColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val showGeneralTab = role == UserRole.STUDENT
    val activeChannel = if (showGeneralTab && selectedTab == 1) "general" else "direct"

    var courseTag by remember { mutableStateOf("TM351") }
    var textInput by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> pickedImageUri = uri }
    )

    val visibleMessages = remember(messages, activeChannel) {
        messages.filter { it.channel == activeChannel }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = strings.chat,
            style = MaterialTheme.typography.titleLarge,
            color = ext.textPrimary,
            fontWeight = FontWeight.Bold
        )

        if (showGeneralTab) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Direct Instructor Chat") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("General Student Chat") }
                )
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Direct Student–Instructor Chat (No personal phone numbers required)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleMessages, key = { it.id }) { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ext.cardBorder),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (msg.courseTag.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = msg.courseTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.textSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.textPrimary
                        )
                        if (!msg.attachmentBase64.isNullOrBlank()) {
                            val bitmap = remember(msg.attachmentBase64) {
                                runCatching {
                                    val bytes = Base64.decode(msg.attachmentBase64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }.getOrNull()
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Chat image attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pickedImageUri != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = pickedImageUri,
                    contentDescription = "Selected attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text("Image attached", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { pickedImageUri = null }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove attachment")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (activeChannel == "direct") {
                IconButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("chat_attach_image_button")
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach image")
                }
            }
            OutlinedTextField(
                value = courseTag,
                onValueChange = { courseTag = it },
                label = { Text("Course") },
                singleLine = true,
                modifier = Modifier.width(86.dp)
            )
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Message") },
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input")
            )
            IconButton(
                onClick = {
                    onSendMessage(activeChannel, courseTag, textInput, pickedImageUri)
                    textInput = ""
                    pickedImageUri = null
                },
                modifier = Modifier.testTag("chat_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

enum class StudentSubTool {
    HUB,
    CALENDAR,
    STUDY_ASSISTANT,
    FRESHMEN_QA,
    VOICE_NOTES,
    MANUAL_NOTES,
    FACULTY_MATERIALS,
    SETTINGS
}

@Composable
fun StudentToolsHubScreen(
    activeTool: StudentSubTool,
    onSelectTool: (StudentSubTool) -> Unit,
    calendarContent: @Composable () -> Unit,
    studyAssistantContent: @Composable () -> Unit,
    freshmenQaContent: @Composable () -> Unit,
    voiceNotesContent: @Composable () -> Unit,
    manualNotesContent: @Composable () -> Unit,
    facultyMaterialsContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit
) {
    if (activeTool != StudentSubTool.HUB) {
        BackHandler { onSelectTool(StudentSubTool.HUB) }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectTool(StudentSubTool.HUB) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Tools")
                }
                Text("Back to Tools", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            Box(modifier = Modifier.weight(1f)) {
                when (activeTool) {
                    StudentSubTool.CALENDAR -> calendarContent()
                    StudentSubTool.STUDY_ASSISTANT -> studyAssistantContent()
                    StudentSubTool.FRESHMEN_QA -> freshmenQaContent()
                    StudentSubTool.VOICE_NOTES -> voiceNotesContent()
                    StudentSubTool.MANUAL_NOTES -> manualNotesContent()
                    StudentSubTool.FACULTY_MATERIALS -> facultyMaterialsContent()
                    StudentSubTool.SETTINGS -> settingsContent()
                    StudentSubTool.HUB -> Unit
                }
            }
        }
        return
    }

    val ext = LocalUniSyncColors.current
    val tools = listOf(
        Triple(StudentSubTool.CALENDAR, "Calendar & Smart Alarm", Icons.Filled.CalendarMonth),
        Triple(StudentSubTool.STUDY_ASSISTANT, "Study Assistant", Icons.Filled.AutoAwesome),
        Triple(StudentSubTool.FRESHMEN_QA, "Freshmen Q&A", Icons.Filled.HelpOutline),
        Triple(StudentSubTool.VOICE_NOTES, "Voice Notes Recorder", Icons.Filled.Mic),
        Triple(StudentSubTool.MANUAL_NOTES, "My Materials & Writing Space", Icons.Filled.EditNote),
        Triple(StudentSubTool.FACULTY_MATERIALS, "Faculty Lecture Materials", Icons.Filled.FolderOpen),
        Triple(StudentSubTool.SETTINGS, "Account & Settings", Icons.Filled.Settings)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Academic Tools",
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        items(tools, key = { it.first.name }) { (tool, label, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_card_${tool.name.lowercase()}")
                    .clickable { onSelectTool(tool) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun StudyAssistantScreen(
    messages: List<StudyMessage>,
    streaming: Boolean,
    onAsk: (String) -> Unit
) {
    val ext = LocalUniSyncColors.current
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Study Assistant",
            style = MaterialTheme.typography.titleLarge,
            color = ext.textPrimary,
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Ask about any AOU course concept, TMA structure, or exam revision strategy.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            items(messages, key = { it.id }) { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.senderIsUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, ext.cardBorder),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (msg.senderIsUser) MaterialTheme.colorScheme.onPrimaryContainer else ext.textPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Ask a study question") },
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    onAsk(prompt)
                    prompt = ""
                },
                enabled = !streaming && prompt.isNotBlank()
            ) {
                Text(if (streaming) "..." else "Ask")
            }
        }
    }
}

@Composable
fun FreshmenQaScreen(
    aiState: AiUiState,
    onAskAi: (String) -> Unit
) {
    val ext = LocalUniSyncColors.current
    var searchQuery by remember { mutableStateOf("") }
    var customQuestion by remember { mutableStateOf("") }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) SampleAcademicData.freshmenQaList
        else SampleAcademicData.freshmenQaList.filter {
            it.question.contains(searchQuery, true) || it.answer.contains(searchQuery, true) || it.category.contains(searchQuery, true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Freshmen Q&A",
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search AOU guides (LMS, TMA, GPA)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ask a Custom Orientation Question", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customQuestion,
                            onValueChange = { customQuestion = it },
                            label = { Text("Your question") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onAskAi(customQuestion) },
                            enabled = customQuestion.isNotBlank()
                        ) {
                            Text("Ask")
                        }
                    }
                    when (aiState) {
                        is AiUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        is AiUiState.Success -> Text(aiState.text, style = MaterialTheme.typography.bodySmall)
                        else -> Unit
                    }
                }
            }
        }
        items(filtered, key = { it.id }) { qa ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(qa.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(qa.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(qa.answer, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                }
            }
        }
    }
}

@Composable
fun VoiceNotesScreen(
    notes: List<VoiceNoteItem>,
    recording: Boolean,
    playingId: String?,
    onStartRecord: () -> Unit,
    onStopRecord: (String) -> Unit,
    onTogglePlay: (VoiceNoteItem) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val context = LocalContext.current
    var noteTitle by remember { mutableStateOf("") }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) onStartRecord()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Voice Notes",
            style = MaterialTheme.typography.titleLarge,
            color = ext.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Voice Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (recording) {
                            onStopRecord(noteTitle)
                            noteTitle = ""
                        } else {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) onStartRecord() else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(if (recording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (recording) "Stop & Save Recording" else "Record Voice Note")
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { item ->
                val isPlaying = playingId == item.id
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ext.cardBorder),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${item.durationSec}s audio", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                        }
                        IconButton(onClick = { onTogglePlay(item) }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentManualNotesScreen(
    notes: List<ManualNoteItem>,
    onAddNote: (String, String, String, String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val uriHandler = LocalUriHandler.current
    var title by remember { mutableStateOf("") }
    var courseTag by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "My Materials & Writing Space",
            style = MaterialTheme.typography.titleLarge,
            color = ext.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseTag,
                        onValueChange = { courseTag = it },
                        label = { Text("Course") },
                        singleLine = true,
                        modifier = Modifier.weight(0.35f)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title") },
                        singleLine = true,
                        modifier = Modifier.weight(0.65f)
                    )
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your study notes here") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("Optional Link URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onAddNote(title, content, linkUrl, courseTag)
                        title = ""
                        content = ""
                        linkUrl = ""
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Save Note")
                }
            }
        }

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your personal workspace is empty. Add your own notes or study links above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, ext.cardBorder),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onDeleteNote(note.id) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete note", modifier = Modifier.size(16.dp))
                                }
                            }
                            if (note.content.isNotBlank()) {
                                Text(note.content, style = MaterialTheme.typography.bodySmall)
                            }
                            if (note.linkUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { runCatching { uriHandler.openUri(note.linkUrl) } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(note.linkUrl, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFacultyMaterialsReadScreen(materials: List<FacultyMaterialItem>) {
    val ext = LocalUniSyncColors.current
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Faculty Lecture Materials",
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        items(materials, key = { it.id }) { mat ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${mat.courseCode} • ${mat.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(mat.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (mat.description.isNotBlank()) {
                        Text(mat.description, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                    }
                    if (mat.resourceUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { runCatching { uriHandler.openUri(mat.resourceUrl) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mat.resourceUrl, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    displayName: String,
    email: String,
    settings: LocalUserSettings,
    isRtl: Boolean,
    strings: UiStrings,
    onPhotoPicked: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectFontScale: (FontScaleOption) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectBranch: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val ext = LocalUniSyncColors.current
    var showLangDialog by remember { mutableStateOf(false) }
    var showPlatformsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = strings.settings,
            style = MaterialTheme.typography.titleLarge,
            color = ext.textPrimary,
            fontWeight = FontWeight.Bold
        )

        // Profile Card (ONLY a single small camera badge on avatar — NO custom app icon controls)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileAvatarWithSingleCamera(
                    displayName = displayName,
                    photoUri = settings.profilePhotoUri,
                    onPhotoPicked = onPhotoPicked
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                    Text(
                        text = if (settings.role == UserRole.INSTRUCTOR) strings.instructorRole else strings.studentRole,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Appearance: Dark Mode + Font Size Scale
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(strings.darkMode, style = MaterialTheme.typography.titleSmall)
                    }
                    Switch(
                        checked = settings.darkMode,
                        onCheckedChange = onToggleDarkMode,
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.FormatSize, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Font Size / Text Scale", style = MaterialTheme.typography.titleSmall)
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontScaleOption.entries.forEach { option ->
                        FilterChip(
                            selected = settings.fontScale == option,
                            onClick = { onSelectFontScale(option) },
                            label = { Text(if (isRtl) option.labelAr else option.labelEn) },
                            modifier = Modifier.testTag("font_scale_${option.key}")
                        )
                    }
                }
            }
        }

        // Language (20 Languages) & AOU Branch
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showLangDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${strings.language}: ${LocalizationData.find(settings.languageCode).nativeName}")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (isRtl) "فروع الجامعة في المملكة العربية السعودية" else "AOU Branches — Saudi Arabia",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SampleAcademicData.saudiBranches.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { branch ->
                                FilterChip(
                                    selected = settings.aouBranch == branch,
                                    onClick = { onSelectBranch(branch) },
                                    label = {
                                        Text(
                                            text = branch.substringBefore(" —"),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("branch_chip_${branch.substringBefore(" ").lowercase()}")
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Text(
                    text = if (isRtl) "الفروع الإقليمية الأخرى" else "Other Regional AOU Branches",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.textSecondary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleAcademicData.regionalBranches.forEach { branch ->
                        FilterChip(
                            selected = settings.aouBranch == branch,
                            onClick = { onSelectBranch(branch) },
                            label = { Text(branch) }
                        )
                    }
                }
            }
        }

        // Download & Platforms (App Store, Google Play, Windows, macOS)
        OutlinedButton(
            onClick = { showPlatformsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.platformsInstall)
        }

        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.signOut)
        }
    }

    if (showLangDialog) {
        LanguagePickerDialog(
            currentCode = settings.languageCode,
            onSelect = onSelectLanguage,
            onDismiss = { showLangDialog = false }
        )
    }

    if (showPlatformsDialog) {
        PlatformsDownloadDialog(onDismiss = { showPlatformsDialog = false })
    }
}
