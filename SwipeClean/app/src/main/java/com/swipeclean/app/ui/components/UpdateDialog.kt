package com.swipeclean.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swipeclean.app.data.AppLanguage
import com.swipeclean.app.data.AppStrings
import com.swipeclean.app.data.AppUpdateInfo
import com.swipeclean.app.data.DownloadState
import com.swipeclean.app.ui.theme.DeleteRed
import com.swipeclean.app.ui.theme.KeepGreen
import java.io.File

@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadState: DownloadState,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onDownloadClick: (AppUpdateInfo) -> Unit,
    onInstallClick: (File) -> Unit,
    onOpenBrowserFallback: (String) -> Unit
) {
    val canDismiss = !updateInfo.isMandatory && downloadState !is DownloadState.Downloading

    Dialog(onDismissRequest = {
        if (canDismiss) onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon depending on download state
                val iconColor = when (downloadState) {
                    is DownloadState.Completed -> KeepGreen
                    is DownloadState.Error -> DeleteRed
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (downloadState) {
                                is DownloadState.Completed -> Icons.Default.CheckCircle
                                is DownloadState.Error -> Icons.Default.ErrorOutline
                                else -> Icons.Default.RocketLaunch
                            },
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                val dialogTitle = when (downloadState) {
                    is DownloadState.Completed -> AppStrings.downloadComplete(currentLanguage)
                    is DownloadState.Error -> AppStrings.downloadError(currentLanguage)
                    else -> updateInfo.title(currentLanguage)
                }

                Text(
                    text = dialogTitle,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Version Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "v${updateInfo.versionName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Area based on DownloadState
                when (downloadState) {
                    is DownloadState.Idle -> {
                        // Changelog Box
                        val changelog = updateInfo.changelog(currentLanguage)
                        if (changelog.isNotBlank()) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "ما الجديد في هذا الإصدار:" else "What's new in this release:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = changelog,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Download in-app button
                        Button(
                            onClick = { onDownloadClick(updateInfo) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.downloadUpdate(currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    is DownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.downloadingUpdate(currentLanguage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(downloadState.progress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${formatDownloadSize(downloadState.downloadedBytes)} / ${if (downloadState.totalBytes > 0) formatDownloadSize(downloadState.totalBytes) else "..."}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    is DownloadState.Completed -> {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.downloadReadyToInstall(currentLanguage),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Install Button
                        Button(
                            onClick = { onInstallClick(downloadState.apkFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = KeepGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.installUpdate(currentLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }

                    is DownloadState.Error -> {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = downloadState.message,
                            fontSize = 12.sp,
                            color = DeleteRed,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onDownloadClick(updateInfo) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.retry(currentLanguage), fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onOpenBrowserFallback(updateInfo.downloadUrl) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.downloadViaBrowser(currentLanguage), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Dismiss / Later Button
                if (canDismiss) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC) "لاحقاً" else "Later",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDownloadSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
