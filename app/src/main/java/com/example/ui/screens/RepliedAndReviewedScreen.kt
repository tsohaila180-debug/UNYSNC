package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.UiStrings
import com.example.model.AcademicEmail
import com.example.model.RepeatedQuestionCluster
import com.example.model.UserRole
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.LocalUniSyncColors
import com.example.ui.theme.StatusCompleted

@Composable
fun RepliedAndReviewedScreen(
    role: UserRole,
    repeatedQuestions: List<RepeatedQuestionCluster>,
    answeredQuestionIds: Set<String>,
    answeredQuestionReplies: Map<String, String>,
    emails: List<AcademicEmail>,
    sentEmailReplies: Map<String, String>,
    languageCode: String,
    translatedEmails: Map<String, Pair<String, String>>,
    isRtl: Boolean,
    strings: UiStrings,
    onOpenEmail: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val ext = LocalUniSyncColors.current
    val answeredClusters = repeatedQuestions.filter { it.id in answeredQuestionIds }
    val reviewedEmails = emails.filter { it.replied || it.viewed }
        .sortedByDescending { it.timestampMillis }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("replied_reviewed_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.home)
            }
            Column {
                Text(
                    text = strings.repliedReviewed,
                    style = MaterialTheme.typography.titleLarge,
                    color = ext.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (role == UserRole.INSTRUCTOR) {
                        if (isRtl) "الأسئلة المتكررة المجاب عنها والرسائل المردود عليها"
                        else "Answered repeated questions & replied/reviewed emails"
                    } else {
                        if (isRtl) "الرسائل التي تم الرد عليها أو الاطلاع عليها"
                        else "Emails you have replied to or opened"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.textSecondary
                )
            }
        }

        if (answeredClusters.isEmpty() && reviewedEmails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRtl) "لا توجد عناصر مردود عليها أو مقروءة بعد." else "No replied or reviewed items yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (role == UserRole.INSTRUCTOR && answeredClusters.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isRtl) "الأسئلة الطلابية المجاب عنها" else "Answered Repeated Questions (${answeredClusters.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(answeredClusters, key = { "hist_${it.id}" }) { cluster ->
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
                                            text = "${cluster.courseCode} • ${cluster.askCount} students",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusCompleted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (isRtl) "تم الرد" else "Answered",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusCompleted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = cluster.topic,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Students: ${cluster.studentEmails.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                                val sentUnified = answeredQuestionReplies[cluster.id]
                                if (!sentUnified.isNullOrBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, ext.cardBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = if (isRtl) "الرد الموحد المرسل:" else "Sent Unified Reply:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = sentUnified,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ext.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (reviewedEmails.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isRtl) {
                                "الرسائل المردود عليها والمقروءة (${reviewedEmails.size})"
                            } else {
                                "Replied & Reviewed Emails (${reviewedEmails.size})"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(reviewedEmails, key = { "rev_${it.id}" }) { email ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reviewed_email_row_${email.id}")
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
                                    Text(
                                        text = email.senderName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ext.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = if (email.replied) StatusCompleted.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (email.replied) Icons.Filled.DoneAll else Icons.Filled.MarkEmailRead,
                                                contentDescription = null,
                                                tint = if (email.replied) StatusCompleted else ext.textSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (email.replied) {
                                                    if (isRtl) "تم الرد" else "Replied"
                                                } else {
                                                    if (isRtl) "مقروء" else "Reviewed"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (email.replied) StatusCompleted else ext.textSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                val displaySubject = translatedEmails["${email.id}_$languageCode"]?.first ?: email.subject
                                Text(
                                    text = displaySubject,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PriorityBadge(priority = email.priority, isRtl = isRtl)
                                    Text(
                                        text = "${email.courseTag} • ${email.receivedAtLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.textSecondary
                                    )
                                }

                                val replySaved = sentEmailReplies[email.id]
                                if (!replySaved.isNullOrBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, ext.cardBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Reply: $replySaved",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.textSecondary,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(8.dp)
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
}
