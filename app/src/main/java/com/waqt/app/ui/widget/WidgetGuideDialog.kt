package com.waqt.app.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.waqt.app.R
import com.waqt.app.ui.theme.Midnight900
import com.waqt.app.ui.theme.Slate200
import com.waqt.app.ui.theme.Slate400
import com.waqt.app.ui.theme.Slate700
import com.waqt.app.ui.theme.Slate800
import com.waqt.app.ui.theme.WaqtCardBorder
import com.waqt.app.ui.theme.WaqtCoral
import com.waqt.app.ui.theme.WaqtForestGreen
import com.waqt.app.ui.theme.WaqtMintCream
import com.waqt.app.ui.theme.WaqtSage
import com.waqt.app.ui.theme.WaqtTeal
import com.waqt.app.ui.theme.WaqtTextMuted
import com.waqt.app.ui.theme.WaqtTextSecondary

object WidgetPinHelper {
    fun isPinningSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            appWidgetManager?.isRequestPinAppWidgetSupported == true
        } else {
            false
        }
    }

    fun requestPin(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val provider = ComponentName(context, WaqtWidgetReceiver::class.java)
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}

@Composable
fun WidgetGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPinSupported = WidgetPinHelper.isPinningSupported(context)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(WaqtTeal.copy(alpha = 0.5f), Slate700.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(26.dp)
                ),
            color = Slate800,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WaqtTeal.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = WaqtTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HOME SCREEN WIDGET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaqtTeal,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Glance at Prayer Times",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Keep track of Azan times and countdowns right from your home screen without opening the app.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WaqtTextSecondary,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Widget Preview Image Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Midnight900)
                        .border(1.dp, WaqtCardBorder, RoundedCornerShape(18.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_widget_preview),
                        contentDescription = "Waqt Home Widget Preview",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "HOW TO ADD TO HOME SCREEN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaqtSage,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Installation Steps
                WidgetStepRow(
                    stepNumber = "1",
                    title = "Hold Home Screen",
                    instruction = "Touch & hold any empty space on your phone's Home Screen."
                )

                WidgetStepRow(
                    stepNumber = "2",
                    title = "Tap Widgets",
                    instruction = "Select the 'Widgets' option from the bottom menu."
                )

                WidgetStepRow(
                    stepNumber = "3",
                    title = "Add 'Waqt'",
                    instruction = "Find Waqt, touch & hold the widget, and drag it to your screen."
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isPinSupported) {
                        Button(
                            onClick = {
                                val success = WidgetPinHelper.requestPin(context)
                                if (success) {
                                    Toast.makeText(context, "Adding Waqt widget...", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaqtTeal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = null,
                                tint = Midnight900,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Widget to Home Screen",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Midnight900
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WaqtSage
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaqtSage.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isPinSupported) "I'll Add Manually" else "Got It",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetStepRow(
    stepNumber: String,
    title: String,
    instruction: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(WaqtTeal.copy(alpha = 0.2f))
                .border(1.dp, WaqtTeal.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaqtTeal
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaqtTextSecondary,
                    lineHeight = 17.sp
                )
            )
        }
    }
}

@Composable
fun WidgetGuideCard(
    modifier: Modifier = Modifier,
    onOpenDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val isPinSupported = WidgetPinHelper.isPinningSupported(context)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, WaqtCardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WaqtTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = WaqtTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Home Screen Widget",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Glanceable next Azan & prayer schedule",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaqtTextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Widget Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Midnight900)
                    .border(1.dp, WaqtCardBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.img_widget_preview),
                    contentDescription = "Waqt Home Widget Preview",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "HOW TO ADD THE WIDGET",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaqtTeal,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            WidgetStepRow(
                stepNumber = "1",
                title = "Hold Home Screen",
                instruction = "Long-press any empty space on your Android home screen."
            )

            WidgetStepRow(
                stepNumber = "2",
                title = "Select Widgets",
                instruction = "Tap 'Widgets' from the popup menu."
            )

            WidgetStepRow(
                stepNumber = "3",
                title = "Place Waqt Widget",
                instruction = "Locate 'Waqt', then hold and place the widget on your screen."
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isPinSupported) {
                Button(
                    onClick = {
                        val success = WidgetPinHelper.requestPin(context)
                        if (success) {
                            Toast.makeText(context, "Adding Waqt widget...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtTeal)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = null,
                        tint = Midnight900,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Widget to Home Screen",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Midnight900
                        )
                    )
                }
            }
        }
    }
}
