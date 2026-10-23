package com.waqt.app.ui.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.waqt.app.domain.model.NotificationSounds
import com.waqt.app.ui.dashboard.DashboardViewModel
import com.waqt.app.ui.theme.Midnight900
import com.waqt.app.ui.theme.Sky400
import com.waqt.app.ui.theme.Slate400
import com.waqt.app.ui.theme.Slate700
import com.waqt.app.ui.theme.Slate800
import com.waqt.app.ui.theme.WaqtCardBorder
import com.waqt.app.ui.theme.WaqtSage
import com.waqt.app.ui.theme.WaqtTeal
import com.waqt.app.ui.theme.WaqtTextMuted
import com.waqt.app.ui.theme.WaqtTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentSoundName = NotificationSounds.ALL_SOUNDS
        .firstOrNull { it.id == uiState.selectedSoundId }?.name ?: "Zen Chime"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Preferences",
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

            // ========================================================
            // 1. HOME SCREEN WIDGET (MOVED TO FIRST AS REQUESTED)
            // ========================================================
            item {
                Text(
                    text = "HOME SCREEN WIDGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Sky400,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                com.waqt.app.ui.widget.WidgetGuideCard()
            }

            // ========================================================
            // 2. NOTIFICATIONS & AUDIO ALERTS (SEPARATE PAGE NAVIGATION)
            // ========================================================
            item {
                Text(
                    text = "NOTIFICATIONS & AUDIO ALERTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Sky400,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToNotifications() },
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaqtCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WaqtTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = WaqtTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Alerts & Tones",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.notificationsEnabled) {
                                    "Enabled • $currentSoundName • 4 Timings"
                                } else {
                                    "Notifications are currently disabled"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.notificationsEnabled) WaqtSage else Slate400
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open notifications",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ========================================================
            // 3. APP UPDATES (GITHUB RELEASES UPDATER)
            // ========================================================
            item {
                Text(
                    text = "APP UPDATES",
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
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaqtCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Version ${com.waqt.app.BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.updateStatusMessage ?: "Check for updates from GitHub",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.availableUpdate?.isUpdateAvailable == true) WaqtTeal else Slate400
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (uiState.isCheckingForUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = WaqtTeal,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (uiState.availableUpdate?.isUpdateAvailable == true) {
                                            viewModel.showUpdateDialog()
                                        } else {
                                            viewModel.checkForUpdates(silent = false)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WaqtTeal),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (uiState.availableUpdate?.isUpdateAvailable == true) "Update" else "Check",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Midnight900
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.simulateUpdateForTesting()
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WaqtSage),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WaqtSage.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preview Update Popup on Dashboard",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // ========================================================
            // 4. CALCULATION METHOD
            // ========================================================
            item {
                Text(
                    text = "CALCULATION METHOD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Sky400,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                val methods = listOf(
                    CalculationMethod.MUSLIM_WORLD_LEAGUE to "Muslim World League (MWL)",
                    CalculationMethod.EGYPTIAN to "Egyptian General Authority",
                    CalculationMethod.KARACHI to "University of Islamic Sciences, Karachi",
                    CalculationMethod.UMM_AL_QURA to "Umm Al-Qura University, Makkah",
                    CalculationMethod.DUBAI to "Dubai Islamic Affairs (UAE)",
                    CalculationMethod.MOON_SIGHTING_COMMITTEE to "Moonsighting Committee Worldwide",
                    CalculationMethod.NORTH_AMERICA to "ISNA (North America)",
                    CalculationMethod.KUWAIT to "Kuwait Ministry of Awqaf",
                    CalculationMethod.QATAR to "Qatar Ministry of Awqaf",
                    CalculationMethod.SINGAPORE to "MUIS (Singapore)",
                    CalculationMethod.OTHER to "Other"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaqtCardBorder)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        methods.forEach { (method, label) ->
                            val isSelected = uiState.calculationMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateCalculationMethod(method) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCalculationMethod(method) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Sky400)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Slate400
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ========================================================
            // 5. ASR MADHAB
            // ========================================================
            item {
                Text(
                    text = "ASR MADHAB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Sky400,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Shafi / Standard Card
                    val isShafi = uiState.madhab == Madhab.SHAFI
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateMadhab(Madhab.SHAFI) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isShafi) Slate700 else Slate800
                        ),
                        border = if (isShafi) androidx.compose.foundation.BorderStroke(1.dp, Sky400) else androidx.compose.foundation.BorderStroke(1.dp, WaqtCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Standard",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shafi'i, Maliki, Hanbali",
                                color = if (isShafi) Sky400 else Slate400,
                                fontSize = 11.sp,
                                fontWeight = if (isShafi) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Hanafi Card
                    val isHanafi = uiState.madhab == Madhab.HANAFI
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateMadhab(Madhab.HANAFI) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHanafi) Slate700 else Slate800
                        ),
                        border = if (isHanafi) androidx.compose.foundation.BorderStroke(1.dp, Sky400) else androidx.compose.foundation.BorderStroke(1.dp, WaqtCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hanafi",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Later Asr Shadow",
                                color = if (isHanafi) Sky400 else Slate400,
                                fontSize = 11.sp,
                                fontWeight = if (isHanafi) FontWeight.Bold else FontWeight.Normal
                            )
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
