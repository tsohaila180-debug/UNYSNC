package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.SampleAcademicData
import com.example.data.UiStrings
import com.example.model.AcademicEmail
import com.example.model.CalendarAlarmEvent
import com.example.ui.components.ImportantUnrepliedEmailBanner
import com.example.ui.components.NextUpCountdownCard
import com.example.ui.components.formatCountdown
import com.example.ui.components.formatEventDateTime
import com.example.ui.theme.LocalUniSyncColors

@Composable
fun StudentHomeScreen(
    nextEvent: CalendarAlarmEvent?,
    events: List<CalendarAlarmEvent>,
    emails: List<AcademicEmail>,
    nowMillis: Long,
    languageCode: String,
    translatedEmails: Map<String, Pair<String, String>>,
    isRtl: Boolean,
    strings: UiStrings,
    onToggleAlarm: (CalendarAlarmEvent) -> Unit,
    onOpenFullCalendar: () -> Unit,
    onOpenEmail: (String) -> Unit,
    onTestRing: (CalendarAlarmEvent) -> Unit
) {
    val ext = LocalUniSyncColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Alert notice for important unreplied email with direct jump to Email Reader
        ImportantUnrepliedEmailBanner(
            emails = emails,
            languageCode = languageCode,
            translatedEmails = translatedEmails,
            isRtl = isRtl,
            strings = strings,
            onOpenEmail = onOpenEmail
        )

        // Minimal Student Home: 1) Next Up + 2) Calendar & Smart Alarm
        NextUpCountdownCard(
            titleHeading = strings.nextUp,
            event = nextEvent,
            nowMillis = nowMillis,
            onOpenDetails = onOpenFullCalendar
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
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.calendarAlarm,
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenFullCalendar,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Manage", style = MaterialTheme.typography.labelMedium)
                    }
                }

                events.take(4).forEach { event ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ext.cardBorder),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ext.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${event.courseCode} • ${formatEventDateTime(event.eventTimeMillis)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onTestRing(event) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NotificationsActive,
                                        contentDescription = "Ring alarm",
                                        tint = ext.highlightAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Switch(
                                    checked = event.alarmEnabled,
                                    onCheckedChange = { onToggleAlarm(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarAndAlarmScreen(
    events: List<CalendarAlarmEvent>,
    emails: List<AcademicEmail>,
    nowMillis: Long,
    selectedRingtoneId: String,
    alarmVolume: Float,
    languageCode: String,
    translatedEmails: Map<String, Pair<String, String>>,
    isRtl: Boolean,
    strings: UiStrings,
    onAddEvent: (String, String, String, Long, Boolean) -> Unit,
    onToggleAlarm: (CalendarAlarmEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onUpdateSoundSettings: (String, Float) -> Unit,
    onPreviewRingtone: (String, Float) -> Unit,
    onTestRing: (CalendarAlarmEvent) -> Unit,
    onOpenEmail: (String) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("assignment") }
    var offsetMinutes by remember { mutableLongStateOf(60L) }
    var alarmEnabled by remember { mutableStateOf(true) }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val categories = listOf("lecture", "exam", "assignment", "office_hour", "personal")
    val timePresets = listOf(
        1L to "1 min",
        60L to "1 hr",
        24L * 60L to "1 day",
        72L * 60L to "3 days"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.calendarAlarm,
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Calendar Alert Notice for important unreplied email
        item {
            ImportantUnrepliedEmailBanner(
                emails = emails,
                languageCode = languageCode,
                translatedEmails = translatedEmails,
                isRtl = isRtl,
                strings = strings,
                onOpenEmail = onOpenEmail
            )
        }

        // Manual Event & Smart Alarm Form
        item {
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
                        text = "Schedule Event & Smart Alarm",
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
                            modifier = Modifier.weight(0.35f)
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Event / Deadline Title") },
                            singleLine = true,
                            modifier = Modifier.weight(0.65f)
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace("_", " ").replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timePresets.forEach { (mins, label) ->
                            FilterChip(
                                selected = offsetMinutes == mins,
                                onClick = { offsetMinutes = mins },
                                label = { Text("In $label") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Enable Continuous Alarm", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                    }
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onAddEvent(
                                title,
                                courseCode,
                                category,
                                System.currentTimeMillis() + offsetMinutes * 60_000L,
                                alarmEnabled
                            )
                            title = ""
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_calendar_event_button")
                    ) {
                        Text("Save Event & Alarm")
                    }
                }
            }
        }

        // Ringtone & Volume Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Alarm Ringtone & Volume",
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SampleAcademicData.ringtones.forEach { (id, label) ->
                            FilterChip(
                                selected = selectedRingtoneId == id,
                                onClick = { onUpdateSoundSettings(id, alarmVolume) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Slider(
                        value = alarmVolume,
                        onValueChange = { onUpdateSoundSettings(selectedRingtoneId, it) }
                    )
                    OutlinedButton(
                        onClick = { onPreviewRingtone(selectedRingtoneId, alarmVolume) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Alarm Tone")
                    }
                }
            }
        }

        // Upcoming Events List
        items(events, key = { it.id }) { event ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${event.courseCode} • ${formatEventDateTime(event.eventTimeMillis)} • ${formatCountdown(event.eventTimeMillis, nowMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onTestRing(event) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = "Trigger alarm now",
                                tint = ext.highlightAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Switch(
                            checked = event.alarmEnabled,
                            onCheckedChange = { onToggleAlarm(event) }
                        )
                        IconButton(
                            onClick = { onDeleteEvent(event.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete event", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
