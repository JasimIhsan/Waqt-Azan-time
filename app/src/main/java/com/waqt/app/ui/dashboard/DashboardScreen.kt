package com.waqt.app.ui.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import com.waqt.app.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Prayer
import com.waqt.app.ui.widget.WidgetGuideDialog
import com.waqt.app.domain.model.NextPrayerInfo
import com.waqt.app.domain.model.PrayerScheduleItem
import com.waqt.app.ui.theme.Amber400
import com.waqt.app.ui.theme.AzanTheme
import com.waqt.app.ui.theme.Emerald400
import com.waqt.app.ui.theme.Midnight900
import com.waqt.app.ui.theme.Sky300
import com.waqt.app.ui.theme.Sky400
import com.waqt.app.ui.theme.Slate200
import com.waqt.app.ui.theme.Slate400
import com.waqt.app.ui.theme.Slate700
import com.waqt.app.ui.theme.Slate800
import com.waqt.app.ui.theme.WaqtActiveCardSurface
import com.waqt.app.ui.theme.WaqtCardBorder
import com.waqt.app.ui.theme.WaqtCardSurface
import com.waqt.app.ui.theme.WaqtCoral
import com.waqt.app.ui.theme.WaqtDarkBg
import com.waqt.app.ui.theme.WaqtForestGreen
import com.waqt.app.ui.theme.WaqtMintCream
import com.waqt.app.ui.theme.WaqtSage
import com.waqt.app.ui.theme.WaqtSky
import com.waqt.app.ui.theme.WaqtTeal
import com.waqt.app.ui.theme.WaqtTextMuted
import com.waqt.app.ui.theme.WaqtTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onPermissionResult(fineGranted || coarseGranted)
    }

    // Exact Alarm & Notification Permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    if (uiState.showWidgetPrompt) {
        WidgetGuideDialog(
            onDismiss = { viewModel.dismissWidgetPrompt() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_waqt_logo),
                            contentDescription = "Waqt Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Waqt",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = WaqtTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.localityName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = WaqtTextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                },
                actions = {
                    // "i" Button -> About Developer Page
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Developer",
                            tint = WaqtSky
                        )
                    }

                    // Settings Button -> Settings & Preferences Page
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = WaqtSage
                        )
                    }

                    // GPS Manual Refresh Button
                    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = LinearEasing)
                        ),
                        label = "rotate"
                    )

                    IconButton(
                        onClick = {
                            if (!uiState.hasLocation) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                viewModel.refreshLocation()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync GPS Location",
                            tint = WaqtTeal,
                            modifier = if (uiState.isSyncingLocation) Modifier.rotate(angle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Midnight900
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Midnight900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Location Permission Banner (if not yet granted)
            if (!uiState.hasLocation) {
                item {
                    PermissionBanner(
                        onRequestPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }
            }

            // Hero Card: Next Azan
            item {
                uiState.nextPrayer?.let { next ->
                    NextAzanHeroCard(
                        nextPrayer = next,
                        countdownText = uiState.countdownText,
                        localityName = uiState.localityName
                    )
                }
            }

            // Update Available Banner Card (if an update is ready)
            if (uiState.availableUpdate?.isUpdateAvailable == true) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.showUpdateDialog() },
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(listOf(WaqtTeal, Sky400))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(WaqtTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = WaqtTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Update Available • v${uiState.availableUpdate?.latestVersion}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to update Waqt to the newest version",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = WaqtSage,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Button(
                                onClick = { viewModel.showUpdateDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = WaqtTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Update",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Midnight900
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Daily Schedule Header
            item {
                val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate200
                        )
                    )
                    Text(
                        text = dateFormat.format(Date()),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Daily Schedule List of Cards
            uiState.schedule?.items?.let { itemsList ->
                items(itemsList) { item ->
                    PrayerScheduleItemCard(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NextAzanHeroCard(
    nextPrayer: NextPrayerInfo,
    countdownText: String,
    localityName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        WaqtTeal.copy(alpha = 0.7f),
                        WaqtSage.copy(alpha = 0.5f),
                        WaqtForestGreen.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF265A46), // Deep Forest Green (#377158)
                            Color(0xFF2E6E56), // Rich Emerald
                            Color(0xFF3F8B7B), // Seafoam Green
                            Color(0xFF56A69B)  // Vibrant Cyan Teal (#69B6AF)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0C1914).copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NEXT AZAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WaqtSage,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        if (nextPrayer.isTomorrow) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WaqtCoral.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TOMORROW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WaqtCoral,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = WaqtMintCream,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prayer Name & Scheduled Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = nextPrayer.prayerName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = localityName,
                            style = MaterialTheme.typography.bodyMedium.copy(color = WaqtMintCream.copy(alpha = 0.9f))
                        )
                    }

                    Text(
                        text = nextPrayer.formattedTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Countdown Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C1914).copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = WaqtSage,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Starts in",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = WaqtMintCream.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerScheduleItemCard(item: PrayerScheduleItem) {
    val isUpcoming = item.isNext
    val containerColor = if (isUpcoming) WaqtActiveCardSurface else WaqtCardSurface
    val borderModifier = if (isUpcoming) {
        Modifier.border(
            width = 1.5.dp,
            color = WaqtTeal,
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = WaqtCardBorder,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val (iconBgColor, iconTint) = when {
        isUpcoming -> Pair(WaqtTeal.copy(alpha = 0.25f), WaqtTeal)
        item.prayer == Prayer.SUNRISE -> Pair(WaqtCoral.copy(alpha = 0.2f), WaqtCoral)
        item.prayer == Prayer.MAGHRIB -> Pair(WaqtCoral.copy(alpha = 0.2f), WaqtCoral)
        item.prayer == Prayer.FAJR -> Pair(WaqtTeal.copy(alpha = 0.2f), WaqtTeal)
        item.prayer == Prayer.DHUHR -> Pair(WaqtSage.copy(alpha = 0.2f), WaqtSage)
        item.prayer == Prayer.ASR -> Pair(Amber400.copy(alpha = 0.2f), Amber400)
        item.prayer == Prayer.ISHA -> Pair(WaqtSky.copy(alpha = 0.2f), WaqtSky)
        else -> Pair(WaqtCardBorder.copy(alpha = 0.4f), WaqtSage)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUpcoming) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(item.prayer),
                        contentDescription = item.name,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        if (isUpcoming) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WaqtSage)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UPCOMING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaqtDarkBg
                                    )
                                )
                            }
                        }
                    }

                    if (item.prayer == Prayer.SUNRISE) {
                        Text(
                            text = "Shurooq",
                            style = MaterialTheme.typography.bodySmall.copy(color = WaqtCoral)
                        )
                    }
                }
            }

            Text(
                text = item.formattedTime,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUpcoming) WaqtSage else WaqtMintCream
                )
            )
        }
    }
}

@Composable
fun PermissionBanner(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Sky400
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location Permission Required",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Waqt requires GPS access to calculate exact solar prayer times for your coordinates and auto-update on displacement.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Sky400, contentColor = Midnight900),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enable Location Access", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getPrayerIcon(prayer: Prayer): ImageVector {
    return when (prayer) {
        Prayer.FAJR -> Icons.Default.WbTwilight
        Prayer.SUNRISE -> Icons.Default.WbSunny
        Prayer.DHUHR -> Icons.Default.WbSunny
        Prayer.ASR -> Icons.Default.WbTwilight
        Prayer.MAGHRIB -> Icons.Default.WbTwilight
        Prayer.ISHA -> Icons.Default.Mosque
        Prayer.NONE -> Icons.Default.Mosque
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    AzanTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NextAzanHeroCard(
                nextPrayer = NextPrayerInfo(
                    prayer = Prayer.FAJR,
                    prayerName = "Fajr",
                    time = Date(),
                    formattedTime = "05:06 AM",
                    isTomorrow = true
                ),
                countdownText = "08h 35m 12s",
                localityName = "Ramanattukara"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleItemPreview() {
    AzanTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrayerScheduleItemCard(
                item = PrayerScheduleItem(
                    prayer = Prayer.FAJR,
                    name = "Fajr",
                    time = Date(),
                    formattedTime = "05:06 AM",
                    isNext = true
                )
            )
            PrayerScheduleItemCard(
                item = PrayerScheduleItem(
                    prayer = Prayer.DHUHR,
                    name = "Dhuhr",
                    time = Date(),
                    formattedTime = "12:15 PM",
                    isNext = false
                )
            )
        }
    }
}
