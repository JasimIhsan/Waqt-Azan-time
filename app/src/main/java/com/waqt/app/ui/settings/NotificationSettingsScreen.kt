package com.waqt.app.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waqt.app.domain.model.NotificationSounds
import com.waqt.app.ui.dashboard.DashboardViewModel
import com.waqt.app.ui.theme.Midnight900
import com.waqt.app.ui.theme.Sky400
import com.waqt.app.ui.theme.Slate200
import com.waqt.app.ui.theme.Slate400
import com.waqt.app.ui.theme.Slate700
import com.waqt.app.ui.theme.Slate800
import com.waqt.app.ui.theme.WaqtTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTonePreview()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications & Tones",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Sky400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Midnight900
                )
            )
        },
        containerColor = Midnight900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ==========================================
            // 1. NOTIFICATIONS MASTER SWITCH
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Play custom tone on Azan and before prayer ends",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        }
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Midnight900,
                                checkedTrackColor = Sky400,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate700
                            )
                        )
                    }
                }
            }

            // ==========================================
            // 2. SEND TEST NOTIFICATION BUTTON
            // ==========================================
            if (uiState.notificationsEnabled) {
                item {
                    Button(
                        onClick = { viewModel.sendTestNotification() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Sky400,
                            contentColor = Midnight900
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Test Notification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // ==========================================
                // 3. NOTIFICATION TIMINGS
                // ==========================================
                item {
                    Text(
                        text = "NOTIFICATION TIMINGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Sky400,
                            letterSpacing = 1.sp
                        )
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            NotificationCheckboxItem(
                                title = "On time of Azan",
                                subtitle = "Fires at exact prayer start moment",
                                checked = uiState.notifyAtAzanTime,
                                onCheckedChange = {
                                    viewModel.updateNotificationAlertPreferences(
                                        it,
                                        uiState.notifyBeforeEnd30Min,
                                        uiState.notifyBeforeEnd15Min,
                                        uiState.notifyBeforeEnd5Min
                                    )
                                }
                            )

                            NotificationCheckboxItem(
                                title = "30 minutes before prayer ends",
                                subtitle = "Prior to next prayer transition / sunrise",
                                checked = uiState.notifyBeforeEnd30Min,
                                onCheckedChange = {
                                    viewModel.updateNotificationAlertPreferences(
                                        uiState.notifyAtAzanTime,
                                        it,
                                        uiState.notifyBeforeEnd15Min,
                                        uiState.notifyBeforeEnd5Min
                                    )
                                }
                            )

                            NotificationCheckboxItem(
                                title = "15 minutes before prayer ends",
                                subtitle = "Intermediate preparation reminder",
                                checked = uiState.notifyBeforeEnd15Min,
                                onCheckedChange = {
                                    viewModel.updateNotificationAlertPreferences(
                                        uiState.notifyAtAzanTime,
                                        uiState.notifyBeforeEnd30Min,
                                        it,
                                        uiState.notifyBeforeEnd5Min
                                    )
                                }
                            )

                            NotificationCheckboxItem(
                                title = "5 minutes before prayer ends",
                                subtitle = "Urgent end-of-prayer warning",
                                checked = uiState.notifyBeforeEnd5Min,
                                onCheckedChange = {
                                    viewModel.updateNotificationAlertPreferences(
                                        uiState.notifyAtAzanTime,
                                        uiState.notifyBeforeEnd30Min,
                                        uiState.notifyBeforeEnd15Min,
                                        it
                                    )
                                }
                            )
                        }
                    }
                }

                // ==========================================
                // 4. 10 MINIMAL NOTIFICATION SOUNDS
                // ==========================================
                item {
                    Text(
                        text = "CUSTOM MINIMAL NOTIFICATION TONES (10 OPTIONS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Sky400,
                            letterSpacing = 1.sp
                        )
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationSounds.ALL_SOUNDS.forEach { sound ->
                            val isSelected = uiState.selectedSoundId == sound.id
                            val isPlaying = uiState.previewPlayingSoundId == sound.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectNotificationSound(sound.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Slate700 else Slate800
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Sky400, Sky400))
                                ) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectNotificationSound(sound.id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = Sky400)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 6.dp)
                                    ) {
                                        Text(
                                            text = sound.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Slate200
                                            )
                                        )
                                        Text(
                                            text = sound.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate400,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    // Play / Stop Preview Button
                                    IconButton(
                                        onClick = { viewModel.playTonePreview(sound.id) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Preview Tone",
                                            tint = if (isPlaying) Sky400 else Slate400
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationCheckboxItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Sky400,
                checkmarkColor = Midnight900
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    fontSize = 11.sp
                )
            )
        }
    }
}
