package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.UiStrings
import com.example.model.AcademicEmail
import com.example.model.AiUiState
import com.example.model.EmailPriority
import com.example.model.ReplyTone
import com.example.ui.components.ImportantUnrepliedEmailBanner
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.LocalUniSyncColors

@Composable
fun InboxScreen(
    emails: List<AcademicEmail>,
    languageCode: String,
    translatedEmails: Map<String, Pair<String, String>>,
    priorityFilter: EmailPriority?,
    sortByPriority: Boolean,
    selectedEmailId: String?,
    summaryState: AiUiState,
    smartReplyDraft: String,
    smartReplyLoading: Boolean,
    selectedTone: ReplyTone,
    isRtl: Boolean,
    strings: UiStrings,
    onSelectFilter: (EmailPriority?) -> Unit,
    onToggleSort: () -> Unit,
    onOpenEmail: (String?) -> Unit,
    onSummarizeEmail: (AcademicEmail) -> Unit,
    onGenerateSmartReply: (AcademicEmail, ReplyTone) -> Unit,
    onDraftChange: (String) -> Unit,
    onSendReply: (AcademicEmail, String) -> Unit,
    onExtractDates: (AcademicEmail) -> Unit,
    onComposeSend: (String, String, String, EmailPriority) -> Unit
) {
    val selectedEmail = emails.firstOrNull { it.id == selectedEmailId }
    var showComposeDialog by remember { mutableStateOf(false) }

    if (selectedEmail != null) {
        BackHandler { onOpenEmail(null) }
        val adaptedPair = if (languageCode == "en") null else translatedEmails["${selectedEmail.id}_$languageCode"]
        FullEmailReaderScreen(
            email = selectedEmail,
            adaptedSubject = adaptedPair?.first,
            adaptedBody = adaptedPair?.second,
            languageCode = languageCode,
            summaryState = summaryState,
            smartReplyDraft = smartReplyDraft,
            smartReplyLoading = smartReplyLoading,
            selectedTone = selectedTone,
            isRtl = isRtl,
            strings = strings,
            onBack = { onOpenEmail(null) },
            onSummarize = { onSummarizeEmail(selectedEmail) },
            onGenerateSmartReply = { tone -> onGenerateSmartReply(selectedEmail, tone) },
            onDraftChange = onDraftChange,
            onSendReply = { onSendReply(selectedEmail, smartReplyDraft) },
            onExtractDates = { onExtractDates(selectedEmail) }
        )
        return
    }

    val ext = LocalUniSyncColors.current
    val visibleEmails = remember(emails, priorityFilter, sortByPriority) {
        val filtered = if (priorityFilter == null) emails else emails.filter { it.priority == priorityFilter }
        if (sortByPriority) {
            filtered.sortedWith(compareBy<AcademicEmail> { it.priority.rank }.thenByDescending { it.timestampMillis })
        } else {
            filtered.sortedByDescending { it.timestampMillis }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row: Title + Compose + Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.inbox,
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onToggleSort,
                    modifier = Modifier.testTag("inbox_sort_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sortByPriority) "Priority" else "Recent",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Button(
                    onClick = { showComposeDialog = true },
                    modifier = Modifier.testTag("inbox_compose_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Compose", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        ImportantUnrepliedEmailBanner(
            emails = emails,
            languageCode = languageCode,
            translatedEmails = translatedEmails,
            isRtl = isRtl,
            strings = strings,
            onOpenEmail = { id -> onOpenEmail(id) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Priority Filter Chips (Identical controls for Student & Instructor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = priorityFilter == null,
                onClick = { onSelectFilter(null) },
                label = { Text(strings.all) },
                modifier = Modifier.testTag("filter_all")
            )
            EmailPriority.entries.forEach { priority ->
                FilterChip(
                    selected = priorityFilter == priority,
                    onClick = { onSelectFilter(if (priorityFilter == priority) null else priority) },
                    label = { Text(if (isRtl) priority.labelAr else priority.labelEn) },
                    modifier = Modifier.testTag("filter_${priority.key}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (visibleEmails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No emails match the selected priority filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleEmails, key = { it.id }) { email ->
                    // Compact Metadata ONLY (Sender, Subject, Timestamp, Course tag, Priority badge — NO body snippet!)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_row_${email.id}")
                            .clickable { onOpenEmail(email.id) },
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (email.unread) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                    }
                                    Text(
                                        text = email.senderName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ext.textPrimary,
                                        fontWeight = if (email.unread) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = email.receivedAtLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                            }

                            val rowSubject = if (languageCode == "en") {
                                email.subject
                            } else {
                                translatedEmails["${email.id}_$languageCode"]?.first ?: email.subject
                            }
                            Text(
                                text = rowSubject,
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary,
                                fontWeight = if (email.unread) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PriorityBadge(priority = email.priority, isRtl = isRtl)
                                if (email.courseTag.isNotBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = email.courseTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ext.textSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showComposeDialog) {
        ComposeEmailDialog(
            onDismiss = { showComposeDialog = false },
            onSend = { to, sub, body, prio ->
                onComposeSend(to, sub, body, prio)
                showComposeDialog = false
            }
        )
    }
}

@Composable
fun FullEmailReaderScreen(
    email: AcademicEmail,
    adaptedSubject: String?,
    adaptedBody: String?,
    languageCode: String,
    summaryState: AiUiState,
    smartReplyDraft: String,
    smartReplyLoading: Boolean,
    selectedTone: ReplyTone,
    isRtl: Boolean,
    strings: UiStrings,
    onBack: () -> Unit,
    onSummarize: () -> Unit,
    onGenerateSmartReply: (ReplyTone) -> Unit,
    onDraftChange: (String) -> Unit,
    onSendReply: () -> Unit,
    onExtractDates: () -> Unit
) {
    val ext = LocalUniSyncColors.current
    val uriHandler = LocalUriHandler.current
    var showOriginal by remember(email.id, languageCode) { mutableStateOf(false) }

    val activeSubject = if (!showOriginal && !adaptedSubject.isNullOrBlank()) adaptedSubject else email.subject
    val activeBody = if (!showOriginal && !adaptedBody.isNullOrBlank()) adaptedBody else email.fullBody

    val links = remember(activeBody) {
        Regex("https?://\\S+").findAll(activeBody).map { it.value.trimEnd('.', ',', ')') }.distinct().toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("email_reader_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToInbox)
                }
                Text(
                    text = strings.backToInbox,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (languageCode != "en" && !adaptedBody.isNullOrBlank()) {
                    FilterChip(
                        selected = !showOriginal,
                        onClick = { showOriginal = !showOriginal },
                        label = {
                            Text(
                                text = if (showOriginal) (if (isRtl) "عرض الترجمة" else "Adapted") else (if (isRtl) "النص الأصلي" else "Original"),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
                PriorityBadge(priority = email.priority, isRtl = isRtl)
            }
        }

        // Complete Email Metadata & Full Body (Automatically adapted to user language)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = activeSubject,
                    style = MaterialTheme.typography.titleMedium,
                    color = ext.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "From: ${email.senderName} <${email.senderEmail}>",
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.textSecondary
                )
                Text(
                    text = "To: ${email.recipientEmail} • ${email.receivedAtLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.textSecondary
                )
                if (email.courseTag.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = email.courseTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                HorizontalDivider(color = ext.cardBorder)
                Text(
                    text = activeBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.textPrimary
                )

                if (links.isNotEmpty()) {
                    HorizontalDivider(color = ext.cardBorder)
                    links.forEach { url ->
                        OutlinedButton(
                            onClick = { runCatching { uriHandler.openUri(url) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions: Summarize Email & Extract Dates to Calendar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSummarize,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("summarize_email_button")
            ) {
                Icon(Icons.Filled.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.summarizeEmail, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onExtractDates,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("extract_dates_button")
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.extractDates, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        when (summaryState) {
            is AiUiState.Loading -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Summarizing email...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            is AiUiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Email Summary",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = summaryState.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            is AiUiState.Error -> {
                Text(summaryState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            else -> Unit
        }

        // Built-In Smart Reply Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.smartReply,
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReplyTone.entries.forEach { tone ->
                        FilterChip(
                            selected = selectedTone == tone,
                            onClick = { onGenerateSmartReply(tone) },
                            label = { Text(if (isRtl) tone.labelAr else tone.labelEn) },
                            modifier = Modifier.testTag("tone_${tone.name.lowercase()}")
                        )
                    }
                }

                Button(
                    onClick = { onGenerateSmartReply(selectedTone) },
                    enabled = !smartReplyLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("generate_smart_reply_button")
                ) {
                    if (smartReplyLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Drafting...")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.smartReply)
                    }
                }

                OutlinedTextField(
                    value = smartReplyDraft,
                    onValueChange = onDraftChange,
                    label = { Text("Editable Reply") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_reply_input")
                )

                Button(
                    onClick = onSendReply,
                    enabled = smartReplyDraft.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_email_reply_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.sendReply)
                }
            }
        }
    }
}

@Composable
private fun ComposeEmailDialog(
    onDismiss: () -> Unit,
    onSend: (String, String, String, EmailPriority) -> Unit
) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(EmailPriority.NORMAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compose Email", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("Recipient Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EmailPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.labelEn) }
                        )
                    }
                }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(to, subject, body, priority) },
                enabled = to.isNotBlank() && subject.isNotBlank() && body.isNotBlank()
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
