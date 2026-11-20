package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReplyAll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.model.CalendarAlarmEvent
import com.example.model.FacultyMaterialItem
import com.example.model.RepeatedQuestionCluster
import com.example.ui.components.ImportantUnrepliedEmailBanner
import com.example.ui.components.NextUpCountdownCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.LocalUniSyncColors
import com.example.ui.theme.StatusCompleted

@Composable
fun InstructorHomeScreen(
    repeatedQuestions: List<RepeatedQuestionCluster>,
    answeredQuestionIds: Set<String>,
    nextEvent: CalendarAlarmEvent?,
    nowMillis: Long,
    emails: List<AcademicEmail>,
    languageCode: String,
    translatedEmails: Map<String, Pair<String, String>>,
    isRtl: Boolean,
    strings: UiStrings,
    onSendUnifiedReply: (RepeatedQuestionCluster, String) -> Unit,
    onOpenNextUpCalendar: () -> Unit,
    onOpenEmail: (String) -> Unit,
    onOpenFullInbox: () -> Unit
) {
    val ext = LocalUniSyncColors.current
    var activeCluster by remember { mutableStateOf<RepeatedQuestionCluster?>(null) }
    var replyText by remember { mutableStateOf("") }

    val activeClusters = remember(repeatedQuestions, answeredQuestionIds) {
        repeatedQuestions.filter { it.id !in answeredQuestionIds }
    }
    val primaryCluster = activeClusters.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // CARD 1: Most Repeated Student Questions (Hides answered groups, keeps Answered History accessible)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("instructor_repeated_questions_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Icon(
                            imageVector = Icons.Filled.QuestionAnswer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.repeatedQuestions,
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (primaryCluster != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${primaryCluster.askCount}x • ${primaryCluster.courseCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (primaryCluster != null) {
                    Text(
                        text = primaryCluster.topic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = primaryCluster.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.textSecondary
                    )

                    Button(
                        onClick = {
                            activeCluster = primaryCluster
                            replyText = if (isRtl) {
                                "أعزائي طلاب مقرر ${primaryCluster.courseCode}،\n\nبخصوص «${primaryCluster.topic}»: يُرجى الالتزام بالدليل المحدّث للمقرر وتعليمات التسليم المنشورة على نظام إدارة التعلم (LMS).\n\nمع أطيب التحيات،\nعضو هيئة التدريس"
                            } else {
                                "Dear ${primaryCluster.courseCode} Students,\n\nRegarding \"${primaryCluster.topic}\": Please follow the updated course guide and submission instructions on the AOU LMS.\n\nBest regards,\nCourse Instructor"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("unified_reply_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReplyAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.unifiedReplyAll)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StatusCompleted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isRtl) {
                                "تم الرد على جميع الأسئلة المتكررة"
                            } else {
                                "All caught up — no pending repeated questions"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // CARD 2: Next Up (Live Countdown)
        NextUpCountdownCard(
            titleHeading = strings.nextUp,
            event = nextEvent,
            nowMillis = nowMillis,
            onOpenDetails = onOpenNextUpCalendar
        )

        // Alert notice for important unreplied student email
        ImportantUnrepliedEmailBanner(
            emails = emails,
            languageCode = languageCode,
            translatedEmails = translatedEmails,
            isRtl = isRtl,
            strings = strings,
            onOpenEmail = onOpenEmail
        )

        // Compact Priority Inbox Preview (Metadata ONLY — no body snippets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.inbox,
                style = MaterialTheme.typography.titleSmall,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onOpenFullInbox) {
                Text("Open Full Inbox")
            }
        }

        emails.sortedBy { it.priority.rank }.take(3).forEach { email ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEmail(email.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = email.senderName,
                            style = MaterialTheme.typography.labelLarge,
                            color = ext.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = email.receivedAtLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                    }
                    val displaySubject = translatedEmails["${email.id}_$languageCode"]?.first ?: email.subject
                    Text(
                        text = displaySubject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriorityBadge(priority = email.priority, isRtl = isRtl)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = email.courseTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    activeCluster?.let { cluster ->
        AlertDialog(
            onDismissRequest = { activeCluster = null },
            title = {
                Text(
                    text = "${strings.unifiedReplyAll} (${cluster.courseCode})",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recipients: ${cluster.askCount} students asking about ${cluster.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.textSecondary
                    )
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Editable Unified Reply") },
                        minLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unified_reply_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendUnifiedReply(cluster, replyText)
                        activeCluster = null
                    },
                    enabled = replyText.isNotBlank(),
                    modifier = Modifier.testTag("confirm_unified_reply_button")
                ) {
                    Text(strings.sendReply)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeCluster = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun InstructorMaterialsScreen(
    materials: List<FacultyMaterialItem>,
    strings: UiStrings,
    onPublish: (String, String, String, String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val uriHandler = LocalUriHandler.current

    var courseCode by remember { mutableStateOf("TM351") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var resourceUrl by remember { mutableStateOf("") }
    var materialType by remember { mutableStateOf("slides") }
    val types = listOf("slides", "link", "recording", "assignment")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = strings.materials,
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
                Text(
                    text = "Publish Lecture Material or Online Link",
                    style = MaterialTheme.typography.titleSmall,
                    color = ext.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course") },
                        singleLine = true,
                        modifier = Modifier.weight(0.38f)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.weight(0.62f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Summary") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = resourceUrl,
                    onValueChange = { resourceUrl = it },
                    label = { Text("Resource URL (LMS / Teams / Drive)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = materialType == t,
                            onClick = { materialType = t },
                            label = { Text(t.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Button(
                    onClick = {
                        onPublish(courseCode, title, description, resourceUrl, materialType)
                        title = ""
                        description = ""
                        resourceUrl = ""
                    },
                    enabled = courseCode.isNotBlank() && title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("publish_material_button")
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish to Course")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${mat.courseCode} • ${mat.materialType.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(mat.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete material", modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = mat.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (mat.description.isNotBlank()) {
                            Text(
                                text = mat.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                        if (mat.resourceUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { runCatching { uriHandler.openUri(mat.resourceUrl) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mat.resourceUrl,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
