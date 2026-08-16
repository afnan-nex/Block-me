package com.blockme.feature.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.blockme.core.ui.theme.DarkBg
import com.blockme.core.ui.theme.DarkSurface
import com.blockme.core.ui.theme.ErrorRed
import com.blockme.core.ui.theme.SuccessGreen
import com.blockme.core.ui.theme.Violet

/**
 * Permission gate screen — shown on first launch and whenever a mandatory permission is missing.
 *
 * Rules:
 * - Go button is disabled until ALL mandatory permissions are granted
 * - Each permission row shows current status and a button to open system settings
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun PermissionGateScreen(
    onAllGranted: () -> Unit,
    viewModel: PermissionGateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh permissions every time the screen is resumed (user returns from settings)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshPermissions()
        }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D1A), Color(0xFF12122B))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App icon area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Violet.copy(alpha = 0.4f), Color.Transparent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Violet,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Block me",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Setup Permissions",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Violet.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grant all permissions below to enable the full lockdown experience.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permission cards
            state.permissions.forEach { permStatus ->
                PermissionCard(
                    status = permStatus,
                    onGrantClick = {
                        val intent = viewModel.getIntentForPermission(permStatus.item)
                        settingsLauncher.launch(intent)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Go button
            Button(
                onClick = { if (state.allGranted) onAllGranted() },
                enabled = state.allGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Violet,
                    disabledContainerColor = Violet.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = if (state.allGranted) "Go →" else "Grant all permissions above to continue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    status: PermissionStatus,
    onGrantClick: () -> Unit
) {
    val (title, description) = permissionLabels(status.item)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (status.isGranted) SuccessGreen.copy(alpha = 0.15f)
                        else ErrorRed.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (status.isGranted) Icons.Filled.Check else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (status.isGranted) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.55f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (status.isGranted) "✓ Granted" else "✗ Not Granted",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (status.isGranted) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Grant button (only shown when not granted)
            AnimatedVisibility(visible = !status.isGranted) {
                FilledTonalButton(
                    onClick = onGrantClick,
                    modifier = Modifier,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Violet.copy(alpha = 0.2f),
                        contentColor = Violet
                    )
                ) {
                    Text("Grant", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun permissionLabels(item: PermissionItem): Pair<String, String> = when (item) {
    PermissionItem.OVERLAY ->
        "Display Over Other Apps" to "Required to show the lockdown timer above all apps."
    PermissionItem.ACCESSIBILITY ->
        "Accessibility Service" to "Detects Home, Back & Recents to lock screen immediately."
    PermissionItem.DEVICE_ADMIN ->
        "Device Admin — Required / Must" to "Required to lock the screen when a blocked action is detected."
    PermissionItem.BATTERY_OPTIMIZATION ->
        "Ignore Battery Optimizations" to "Keeps the focus timer running reliably in the background."
    PermissionItem.NOTIFICATIONS ->
        "Notifications" to "Shows the active focus session status notification."
    PermissionItem.EXACT_ALARM ->
        "Exact Alarms" to "Starts scheduled focus sessions at the exact configured time."
}
