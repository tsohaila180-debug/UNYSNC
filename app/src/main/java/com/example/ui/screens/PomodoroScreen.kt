package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UiStrings
import com.example.ui.theme.LocalUniSyncColors
import kotlinx.coroutines.delay
import java.util.Locale

private enum class CompanionAction {
    WALKING_IN,
    STUDYING,
    BREAK_SIP
}

@Composable
fun PomodoroScreen(
    durationMinutes: Int,
    secondsRemaining: Int,
    running: Boolean,
    isBreak: Boolean,
    totalMinutesStudied: Int,
    companionGender: String,
    milestoneAwardMinutes: Int?,
    isRtl: Boolean,
    strings: UiStrings,
    onSelectDuration: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onStartBreak: () -> Unit,
    onExitFocus: () -> Unit,
    onSelectGender: (String) -> Unit,
    onDismissMilestone: () -> Unit
) {
    val ext = LocalUniSyncColors.current
    val context = LocalContext.current
    val activity = context as? Activity
    val isFocusLocked = running && !isBreak

    // Strict OS-level lock enforcement & screen awake while Pomodoro focus session runs
    BackHandler(enabled = isFocusLocked) { }

    DisposableEffect(isFocusLocked, activity) {
        if (isFocusLocked && activity != null) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            runCatching { activity.startLockTask() }
        }
        onDispose {
            if (activity != null) {
                activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                runCatching { activity.stopLockTask() }
            }
        }
    }

    var walkingIn by remember { mutableStateOf(true) }
    LaunchedEffect(companionGender) {
        walkingIn = true
        delay(1700L)
        walkingIn = false
    }

    val companionAction = when {
        walkingIn -> CompanionAction.WALKING_IN
        isBreak -> CompanionAction.BREAK_SIP
        else -> CompanionAction.STUDYING
    }

    val mins = secondsRemaining / 60
    val secs = secondsRemaining % 60
    val clockText = String.format(Locale.US, "%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFocusLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.pomodoro,
                        style = MaterialTheme.typography.titleLarge,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "$totalMinutesStudied min",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Girl / Boy Companion Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = companionGender == "girl",
                        onClick = { onSelectGender("girl") },
                        leadingIcon = { Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (isRtl) "طالبة" else "Girl") },
                        modifier = Modifier.testTag("companion_girl_chip")
                    )
                    FilterChip(
                        selected = companionGender == "boy",
                        onClick = { onSelectGender("boy") },
                        leadingIcon = { Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (isRtl) "طالب" else "Boy") },
                        modifier = Modifier.testTag("companion_boy_chip")
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = strings.studyFocus,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Main Card: Animated Cartoon Companion (LEFT of clock) + Clock + Exit Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnimatedStudyCompanion(
                            gender = companionGender,
                            action = companionAction,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            accentColor = ext.highlightAccent,
                            modifier = Modifier.size(width = 132.dp, height = 136.dp)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBreak) strings.breakTime else strings.studyFocus,
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.highlightAccent
                            )
                            Text(
                                text = clockText,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ext.textPrimary,
                                modifier = Modifier.testTag("pomodoro_clock_text")
                            )
                        }
                    }

                    if (!isFocusLocked) {
                        val durations = listOf(25, 30, 45, 50, 60)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            durations.forEach { d ->
                                FilterChip(
                                    selected = durationMinutes == d && !isBreak,
                                    onClick = { onSelectDuration(d) },
                                    label = { Text("${d}m") }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onStartFocus,
                                enabled = !running,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("start_pomodoro_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRtl) "بدء التركيز" else "Start Focus")
                            }
                            OutlinedButton(
                                onClick = onStartBreak,
                                enabled = !running,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("start_break_button")
                            ) {
                                Icon(Icons.Filled.LocalCafe, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRtl) "استراحة 5 د" else "5m Break")
                            }
                        }
                    }

                    if (running) {
                        OutlinedButton(
                            onClick = {
                                if (activity != null) {
                                    activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    runCatching { activity.stopLockTask() }
                                }
                                onExitFocus()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("exit_pomodoro_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.exitPomodoro)
                        }
                    }
                }
            }

            // Minimal 30-Minute Milestone Progress Bar (zero explanatory prose)
            val progressIntoNext30 = totalMinutesStudied % 30
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ext.cardBorder),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = ext.highlightAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isRtl) "وسام 30 دقيقة" else "30m Milestone",
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "$progressIntoNext30 / 30",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressIntoNext30 / 30f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }
        }
    }

    if (milestoneAwardMinutes != null) {
        AlertDialog(
            onDismissRequest = onDismissMilestone,
            icon = {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = ext.highlightAccent,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isRtl) "إنجاز $milestoneAwardMinutes دقيقة!" else "$milestoneAwardMinutes-Minute Milestone!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(onClick = onDismissMilestone) {
                    Text(if (isRtl) "متابعة" else "Continue")
                }
            }
        )
    }
}

@Composable
private fun AnimatedStudyCompanion(
    gender: String,
    action: CompanionAction,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val walkOffset by animateFloatAsState(
        targetValue = if (action == CompanionAction.WALKING_IN) -18f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "companion_walk"
    )
    val inf = rememberInfiniteTransition(label = "companion_idle")
    val bob by inf.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "companion_bob"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.52f + walkOffset
        val cy = size.height * 0.5f + bob

        if (action != CompanionAction.WALKING_IN) {
            drawRoundRect(
                color = Color(0xFFD6C7B2),
                topLeft = Offset(cx - 46f, cy + 28f),
                size = Size(92f, 12f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        } else {
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(cx - 52f, cy + 16f),
                size = Size(24f, 30f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawCircle(
                color = Color(0xFF334155),
                radius = 4.5f,
                center = Offset(cx - 45f, cy + 48f)
            )
            drawCircle(
                color = Color(0xFF334155),
                radius = 4.5f,
                center = Offset(cx - 34f, cy + 48f)
            )
            drawLine(
                color = Color(0xFF475569),
                start = Offset(cx - 30f, cy + 18f),
                end = Offset(cx - 12f, cy + 8f),
                strokeWidth = 3f
            )
        }

        val outfitColor = if (gender == "girl") primaryColor else Color(0xFF28558A)
        drawRoundRect(
            color = outfitColor,
            topLeft = Offset(cx - 18f, cy - 2f),
            size = Size(36f, 34f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        val hairColor = Color(0xFF3A281E)
        if (gender == "girl") {
            drawCircle(
                color = hairColor,
                radius = 23f,
                center = Offset(cx, cy - 22f)
            )
            drawRoundRect(
                color = hairColor,
                topLeft = Offset(cx - 21f, cy - 22f),
                size = Size(42f, 24f),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }

        drawCircle(
            color = Color(0xFFF6D5B8),
            radius = 19f,
            center = Offset(cx, cy - 24f)
        )

        drawArc(
            color = hairColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 19f, cy - 43f),
            size = Size(38f, 22f)
        )

        drawCircle(color = Color(0xFF1E293B), radius = 2.6f, center = Offset(cx - 6.5f, cy - 24f))
        drawCircle(color = Color(0xFF1E293B), radius = 2.6f, center = Offset(cx + 6.5f, cy - 24f))
        drawArc(
            color = Color(0xFF9A4E3B),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - 5f, cy - 20f),
            size = Size(10f, 6f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )

        when (action) {
            CompanionAction.STUDYING -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - 22f, cy + 16f),
                    size = Size(44f, 14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(cx, cy + 16f),
                    end = Offset(cx, cy + 30f),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = accentColor,
                    start = Offset(cx + 18f, cy + 12f),
                    end = Offset(cx + 28f, cy + 24f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            CompanionAction.BREAK_SIP -> {
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(cx + 12f, cy + 2f),
                    size = Size(14f, 20f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(cx + 19f, cy + 2f),
                    end = Offset(cx + 23f, cy - 10f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
            CompanionAction.WALKING_IN -> Unit
        }
    }
}
