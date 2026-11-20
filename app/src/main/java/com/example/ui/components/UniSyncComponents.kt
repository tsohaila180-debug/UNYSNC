package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MarkEmailUnread
import com.example.R
import com.example.data.LocalizationData
import com.example.data.SampleAcademicData
import com.example.data.UiStrings
import com.example.model.AcademicEmail
import com.example.model.CalendarAlarmEvent
import com.example.model.EmailPriority
import com.example.ui.theme.LocalUniSyncColors
import com.example.ui.theme.PriorityCriticalBg
import com.example.ui.theme.PriorityCriticalText
import com.example.ui.theme.PriorityHighBg
import com.example.ui.theme.PriorityHighText
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityLowText
import com.example.ui.theme.PriorityNormalBg
import com.example.ui.theme.PriorityNormalText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UniSyncAppIconBadge(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_unisync_foreground_1795173810041),
        contentDescription = "UniSync App Icon",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
    )
}

@Composable
fun ProfileAvatarWithSingleCamera(
    displayName: String,
    photoUri: String,
    onPhotoPicked: (String) -> Unit,
    size: Dp = 68.dp
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { onPhotoPicked(it.toString()) }
        }
    )

    Box(modifier = Modifier.size(size)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            if (photoUri.isNotBlank()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = displayName.trim().take(2).uppercase().ifBlank { "AU" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Single camera badge directly on profile avatar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(26.dp)
                .clip(CircleShape)
                .testTag("profile_camera_button")
                .clickable {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Change profile photo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: EmailPriority, isRtl: Boolean) {
    val (bg, fg, icon) = when (priority) {
        EmailPriority.CRITICAL -> Triple(PriorityCriticalBg, PriorityCriticalText, Icons.Filled.Error)
        EmailPriority.HIGH -> Triple(PriorityHighBg, PriorityHighText, Icons.Filled.PriorityHigh)
        EmailPriority.NORMAL -> Triple(PriorityNormalBg, PriorityNormalText, Icons.Filled.Remove)
        EmailPriority.LOW -> Triple(PriorityLowBg, PriorityLowText, Icons.Filled.ArrowDownward)
    }
    val label = if (isRtl) priority.labelAr else priority.labelEn
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                color = fg,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ImportantUnrepliedEmailBanner(
    emails: List<AcademicEmail>,
    languageCode: String = "en",
    translatedEmails: Map<String, Pair<String, String>> = emptyMap(),
    isRtl: Boolean,
    strings: UiStrings,
    onOpenEmail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val urgentUnreplied = emails
        .filter { !it.replied && (it.priority == EmailPriority.CRITICAL || it.priority == EmailPriority.HIGH) }
        .sortedWith(compareBy<AcademicEmail> { it.priority.rank }.thenByDescending { it.timestampMillis })

    val topEmail = urgentUnreplied.firstOrNull() ?: return
    val displaySubject = if (languageCode == "en") {
        topEmail.subject
    } else {
        translatedEmails["${topEmail.id}_$languageCode"]?.first ?: topEmail.subject
    }
    val isCritical = topEmail.priority == EmailPriority.CRITICAL
    val bannerBg = if (isCritical) PriorityCriticalBg else PriorityHighBg
    val bannerFg = if (isCritical) PriorityCriticalText else PriorityHighText

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("important_unreplied_email_banner")
            .clickable { onOpenEmail(topEmail.id) },
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = BorderStroke(1.dp, bannerFg.copy(alpha = 0.45f)),
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
                    Icon(
                        imageVector = Icons.Filled.MarkEmailUnread,
                        contentDescription = strings.urgentEmailAlert,
                        tint = bannerFg,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (urgentUnreplied.size > 1) {
                            "${strings.urgentEmailAlert} (${urgentUnreplied.size})"
                        } else {
                            strings.urgentEmailAlert
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = bannerFg,
                        fontWeight = FontWeight.Bold
                    )
                }
                PriorityBadge(priority = topEmail.priority, isRtl = isRtl)
            }

            Text(
                text = displaySubject,
                style = MaterialTheme.typography.titleSmall,
                color = bannerFg,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (topEmail.courseTag.isNotBlank()) {
                        "${topEmail.senderName} • ${topEmail.courseTag}"
                    } else {
                        topEmail.senderName
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = bannerFg.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = strings.tapToOpenReply,
                        style = MaterialTheme.typography.labelSmall,
                        color = bannerFg,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = bannerFg,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NextUpCountdownCard(
    titleHeading: String,
    event: CalendarAlarmEvent?,
    nowMillis: Long,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalUniSyncColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("next_up_card")
            .clickable { onOpenDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ext.cardBorder),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = titleHeading,
                        tint = ext.highlightAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = titleHeading,
                        style = MaterialTheme.typography.labelLarge,
                        color = ext.highlightAccent
                    )
                }
                if (event != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.courseCode.ifBlank { "AOU" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (event == null) {
                Text(
                    text = "No upcoming scheduled sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.textSecondary
                )
            } else {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ext.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCountdown(event.eventTimeMillis, nowMillis),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = "Alarm status",
                            tint = if (event.alarmEnabled) MaterialTheme.colorScheme.primary else ext.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = formatEventDateTime(event.eventTimeMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    currentCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language (20 Languages)",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(LocalizationData.languages, key = { it.code }) { lang ->
                    val selected = lang.code == currentCode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelect(lang.code)
                                onDismiss()
                            },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "${lang.englishName} • ${if (lang.rtl) "RTL" else "LTR"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun PlatformsDownloadDialog(onDismiss: () -> Unit) {
    val ext = LocalUniSyncColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "UniSync Across All Platforms",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SampleAcademicData.platformDownloads, key = { it.id }) { item ->
                    val icon = when (item.id) {
                        "google_play" -> Icons.Filled.PhoneAndroid
                        "app_store" -> Icons.Filled.PhoneIphone
                        "windows" -> Icons.Filled.DesktopWindows
                        else -> Icons.Filled.LaptopMac
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ext.cardBorder),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = item.name,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.instructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun FullScreenAlarmOverlay(
    event: CalendarAlarmEvent,
    onSnooze5Min: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60B0F17))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("alarm_overlay_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = PriorityCriticalBg,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Alarm ringing",
                            tint = PriorityCriticalText,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "UniSync Smart Alarm",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.courseCode} • ${event.category.replace("_", " ").uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze5Min,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("alarm_snooze_button")
                    ) {
                        Icon(Icons.Filled.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Snooze 5 min")
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PriorityCriticalText),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("alarm_dismiss_button")
                    ) {
                        Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

fun formatCountdown(targetMillis: Long, nowMillis: Long): String {
    val diff = (targetMillis - nowMillis).coerceAtLeast(0L) / 1000L
    if (diff == 0L) return "Starting now"
    val days = diff / 86400L
    val hours = (diff % 86400L) / 3600L
    val mins = (diff % 3600L) / 60L
    val secs = diff % 60L
    return if (days > 0) {
        "${days}d ${hours}h ${mins}m ${secs}s"
    } else {
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    }
}

fun formatEventDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
