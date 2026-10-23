package com.waqt.app.ui.about

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.waqt.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waqt.app.ui.theme.Midnight900
import com.waqt.app.ui.theme.Sky300
import com.waqt.app.ui.theme.Sky400
import com.waqt.app.ui.theme.Slate200
import com.waqt.app.ui.theme.Slate400
import com.waqt.app.ui.theme.Slate700
import com.waqt.app.ui.theme.Slate800
import com.waqt.app.ui.theme.WaqtTeal
import com.waqt.app.ui.theme.WaqtSage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About & Contact",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ==========================================
            // 1. APPLICATION HERO CARD
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Sky400.copy(alpha = 0.5f), Slate700)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Slate800)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Slate800, Midnight900.copy(alpha = 0.9f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_waqt_logo),
                                contentDescription = "Waqt Logo",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Waqt",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )

                            Text(
                                text = "Version ${com.waqt.app.BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Sky400,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "A battery-efficient, offline-first native Android app and Jetpack Glance widget providing precise prayer timings, custom minimal acoustic alerts, and real-time GPS displacement tracking.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate200,
                                    lineHeight = 20.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. HOME SCREEN WIDGET GUIDE CARD
            // ==========================================
            item {
                com.waqt.app.ui.widget.WidgetGuideCard()
            }

            // ==========================================
            // 3. KEY APP FEATURES SUMMARY
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "APP ARCHITECTURE & FEATURES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Sky400,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FeatureRow(
                            title = "Offline Astronomical Calculations",
                            description = "Powered by Batoul Apps Adhan solar engine without needing network connectivity."
                        )

                        FeatureRow(
                            title = "Zero-Polling Glance Widget",
                            description = "Updates strictly on exact prayer transitions and 5 km displacement to maximize battery life."
                        )

                        FeatureRow(
                            title = "Minimal Sound Alerts",
                            description = "Notifies on time of Azan as well as 30m, 15m, and 5m before the prayer window ends."
                        )
                    }
                }
            }

            // ==========================================
            // 3. DEVELOPER & CONTACT CARD
            // ==========================================
            item {
                Text(
                    text = "DEVELOPER & CONTACT",
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
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Sky400.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Sky400,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Jasim Ihsan M",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Software Engineer • Malappuram, Kerala",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate400
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "If you have feedback, bug reports, feature requests, or would like to connect for collaboration, feel free to reach out via portfolio or email.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate200,
                                lineHeight = 19.sp
                            )
                        )
                    }
                }
            }

            // ==========================================
            // 4. ACTION & PORTFOLIO BUTTONS
            // ==========================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Primary Portfolio Link
                    Button(
                        onClick = { openUrl("https://www.jasimihsan.in/") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Sky400,
                            contentColor = Midnight900
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Visit Portfolio: jasimihsan.in",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Email Link
                    ContactLinkCard(
                        title = "Email",
                        subtitle = "jasimihsan1234@gmail.com",
                        icon = Icons.Default.Email,
                        onClick = { openUrl("mailto:jasimihsan1234@gmail.com") }
                    )

                    // LinkedIn Link
                    ContactLinkCard(
                        title = "LinkedIn",
                        subtitle = "linkedin.com/in/jasim-ihsan-m",
                        icon = Icons.Default.Language,
                        onClick = { openUrl("https://linkedin.com/in/jasim-ihsan-m") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(
    title: String,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate400,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(start = 10.dp, top = 2.dp)
        )
    }
}

@Composable
private fun ContactLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Sky400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
