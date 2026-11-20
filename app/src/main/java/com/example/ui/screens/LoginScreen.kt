package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthHelper
import com.example.data.LocalizationData
import com.example.data.MicrosoftGraphClient
import com.example.data.UiStrings
import com.example.model.UserRole
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.PlatformsDownloadDialog
import com.example.ui.components.UniSyncAppIconBadge
import com.example.ui.theme.LocalUniSyncColors

@Composable
fun LoginScreen(
    selectedRole: UserRole,
    darkMode: Boolean,
    languageCode: String,
    strings: UiStrings,
    onSelectRole: (UserRole) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onGoogleSuccess: () -> Unit,
    onMicrosoftCodeSubmitted: (String) -> Unit,
    onDemoLogin: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val ext = LocalUniSyncColors.current

    var googleLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPlatformsDialog by remember { mutableStateOf(false) }
    var showMicrosoftCodeDialog by remember { mutableStateOf(false) }
    var microsoftCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compact Top Utility Bar: Dark Mode, 20 Languages, Platforms/Install
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showPlatformsDialog = true },
                modifier = Modifier.testTag("login_platforms_button"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.platformsInstall, style = MaterialTheme.typography.labelMedium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { showLanguageDialog = true },
                    modifier = Modifier.testTag("login_language_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = strings.language, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = LocalizationData.find(languageCode).nativeName,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { onToggleDarkMode(!darkMode) },
                    modifier = Modifier.testTag("login_dark_mode_button")
                ) {
                    Icon(
                        imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = strings.darkMode,
                        tint = ext.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Clean Branding Header: App Icon + "UniSync" ONLY (no "AI", no university subtitle attached to logo)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            UniSyncAppIconBadge(size = 76.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "UniSync",
                style = MaterialTheme.typography.headlineMedium,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Role",
                    style = MaterialTheme.typography.titleSmall,
                    color = ext.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleChoiceCard(
                        title = strings.studentRole,
                        icon = Icons.Filled.School,
                        selected = selectedRole == UserRole.STUDENT,
                        onClick = { onSelectRole(UserRole.STUDENT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_student_card")
                    )
                    RoleChoiceCard(
                        title = strings.instructorRole,
                        icon = Icons.Filled.Workspaces,
                        selected = selectedRole == UserRole.INSTRUCTOR,
                        onClick = { onSelectRole(UserRole.INSTRUCTOR) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_instructor_card")
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Google Sign-In (Jetpack Credential Manager)
                Button(
                    onClick = {
                        authError = null
                        googleLoading = true
                        GoogleAuthHelper.onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                googleLoading = false
                                onGoogleSuccess()
                            },
                            onAuthError = { msg ->
                                googleLoading = false
                                authError = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                googleLoading = false
                            }
                        )
                    },
                    enabled = !googleLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (googleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.signInGoogle)
                    }
                }

                // Microsoft / Outlook Sign-In (Client ID 8326f723-eb8a-4288-b346-9614eb8608c3)
                OutlinedButton(
                    onClick = {
                        authError = null
                        val authUri = MicrosoftGraphClient.buildAuthorizeUri()
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, authUri))
                            showMicrosoftCodeDialog = true
                        } catch (_: Exception) {
                            showMicrosoftCodeDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("microsoft_sign_in_button")
                ) {
                    Icon(Icons.Filled.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.signInMicrosoft)
                }

                // Demo Account Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("demo_account_button")
                        .clickable { onDemoLogin(selectedRole) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.demoAccount} (${if (selectedRole == UserRole.STUDENT) strings.studentRole else strings.instructorRole})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (authError != null) {
                    Text(
                        text = authError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentCode = languageCode,
            onSelect = onSelectLanguage,
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showPlatformsDialog) {
        PlatformsDownloadDialog(onDismiss = { showPlatformsDialog = false })
    }

    if (showMicrosoftCodeDialog) {
        AlertDialog(
            onDismissRequest = { showMicrosoftCodeDialog = false },
            title = { Text("Microsoft / Outlook Sign-In", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Authorize with Microsoft Entra ID (${MicrosoftGraphClient.CLIENT_ID.take(8)}…). If redirected automatically, the app connects right away; or paste the OAuth code below.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = microsoftCodeInput,
                        onValueChange = { microsoftCodeInput = it },
                        label = { Text("OAuth Authorization Code (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = microsoftCodeInput.trim()
                        showMicrosoftCodeDialog = false
                        if (code.isNotBlank()) {
                            onMicrosoftCodeSubmitted(code)
                        }
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicrosoftCodeDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun RoleChoiceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalUniSyncColors.current
    Surface(
        modifier = modifier
            .height(74.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else ext.cardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) MaterialTheme.colorScheme.primary else ext.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else ext.textPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
