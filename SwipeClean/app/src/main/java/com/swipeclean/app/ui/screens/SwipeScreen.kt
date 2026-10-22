package com.swipeclean.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swipeclean.app.data.*
import com.swipeclean.app.ui.components.FeedbackDialog
import com.swipeclean.app.ui.components.FullscreenMediaDialog
import com.swipeclean.app.ui.components.SwipeCard
import com.swipeclean.app.ui.theme.DeleteRed
import com.swipeclean.app.ui.theme.KeepGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    mediaItems: List<MediaItem>,
    currentIndex: Int,
    trashCount: Int,
    keptCount: Int,
    isLoading: Boolean,
    filterType: FilterType,
    selectedFolder: String?,
    availableFolders: List<String>,
    sortOrder: SortOrder,
    selectedStorageType: StorageType,
    availableStorageTypes: List<StorageTypeInfo>,
    currentLanguage: AppLanguage,
    fullscreenSwipeMode: FullscreenSwipeMode,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onKeepItem: (MediaItem) -> Unit,
    onTrashItem: (MediaItem) -> Unit,
    onIndexChanged: (Int) -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onFilterTypeChanged: (FilterType) -> Unit,
    onStorageTypeChanged: (StorageType) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showFullscreenDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }

    if (showFullscreenDialog && mediaItems.isNotEmpty()) {
        FullscreenMediaDialog(
            mediaItems = mediaItems,
            initialIndex = currentIndex,
            fullscreenSwipeMode = fullscreenSwipeMode,
            onDismiss = { showFullscreenDialog = false },
            onKeepItem = onKeepItem,
            onTrashItem = onTrashItem,
            onIndexChanged = onIndexChanged
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SwipeClean",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Settings Button
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = AppStrings.settings(currentLanguage)
                        )
                    }

                    // Feedback / WhatsApp Button
                    IconButton(onClick = { showFeedbackDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = AppStrings.feedback(currentLanguage),
                            tint = Color(0xFF25D366)
                        )
                    }

                    // Sort Order Button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = AppStrings.sort(currentLanguage))
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            order.label,
                                            fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortOrderChanged(order)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (order == sortOrder) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Trash Bin Action with Badge
                    IconButton(onClick = onNavigateToTrash) {
                        BadgedBox(
                            badge = {
                                if (trashCount > 0) {
                                    Badge(
                                        containerColor = DeleteRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$trashCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = AppStrings.trash(currentLanguage))
                        }
                    }

                    // Gallery Action
                    IconButton(onClick = onNavigateToGallery) {
                        Icon(Icons.Default.GridView, contentDescription = AppStrings.gallery(currentLanguage))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filters Row 1: Media Types (All, Images, Videos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterType.entries.forEach { type ->
                    FilterChip(
                        selected = filterType == type,
                        onClick = { onFilterTypeChanged(type) },
                        label = {
                            Text(
                                text = when (type) {
                                    FilterType.ALL -> AppStrings.all(currentLanguage)
                                    FilterType.IMAGE -> AppStrings.photos(currentLanguage)
                                    FilterType.VIDEO -> AppStrings.videos(currentLanguage)
                                },
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = if (filterType == type) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Folder selector
                if (availableFolders.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        AssistChip(
                            onClick = { expanded = true },
                            label = {
                                Text(
                                    text = selectedFolder ?: AppStrings.allFolders(currentLanguage),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(AppStrings.allFolders(currentLanguage)) },
                                onClick = {
                                    onFolderSelected(null)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                                }
                            )
                            HorizontalDivider()
                            availableFolders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder) },
                                    onClick = {
                                        onFolderSelected(folder)
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (folder == selectedFolder) Icons.Default.FolderSpecial
                                            else Icons.Default.Folder,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Filters Row 2: Storage Selector (Internal, SD Card, USB Drive)
            if (availableStorageTypes.size > 1) {
                var storageMenuExpanded by remember { mutableStateOf(false) }
                val currentStorageInfo = availableStorageTypes.find { it.type == selectedStorageType } ?: availableStorageTypes.first()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AssistChip(
                            onClick = { storageMenuExpanded = true },
                            label = {
                                Text(
                                    text = "${currentStorageInfo.emoji} ${if (currentLanguage == AppLanguage.ARABIC) currentStorageInfo.labelAr else currentStorageInfo.labelEn}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        DropdownMenu(
                            expanded = storageMenuExpanded,
                            onDismissRequest = { storageMenuExpanded = false }
                        ) {
                            availableStorageTypes.forEach { info ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${info.emoji} ${if (currentLanguage == AppLanguage.ARABIC) info.labelAr else info.labelEn} (${info.count})",
                                            fontWeight = if (info.type == selectedStorageType) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onStorageTypeChanged(info.type)
                                        storageMenuExpanded = false
                                    },
                                    leadingIcon = if (info.type == selectedStorageType) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "${AppStrings.kept(currentLanguage)}: $keptCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KeepGreen
                    )
                    Text(
                        "${AppStrings.inTrash(currentLanguage)}: $trashCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeleteRed
                    )
                }
                if (mediaItems.isNotEmpty()) {
                    Text(
                        "${AppStrings.remaining(currentLanguage)}: ${mediaItems.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Preview Area (Fills entire available screen)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (currentLanguage == AppLanguage.ARABIC) "جاري قراءة وتحميل الوسائط..." else "Loading media...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    mediaItems.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = KeepGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                AppStrings.reviewDoneMsg(currentLanguage),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (trashCount > 0) {
                                Text(
                                    if (currentLanguage == AppLanguage.ARABIC)
                                        "لديك $trashCount عناصر في السلة جاهزة للمراجعة أو الحذف النهائي."
                                    else
                                        "You have $trashCount items in trash ready for review or deletion.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onNavigateToTrash,
                                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${AppStrings.emptyTrash(currentLanguage)} ($trashCount)")
                                }
                            } else {
                                Text(
                                    if (currentLanguage == AppLanguage.ARABIC)
                                        "لم تقم باختيار أي ملفات لحذفها."
                                    else
                                        "No items marked for deletion.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    currentIndex < mediaItems.size -> {
                        val currentItem = mediaItems[currentIndex]

                        key(currentItem.id) {
                            SwipeCard(
                                mediaItem = currentItem,
                                onSwipeRight = onSwipeRight,
                                onSwipeLeft = onSwipeLeft,
                                onOpenFullscreen = { showFullscreenDialog = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = KeepGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                AppStrings.reviewDoneMsg(currentLanguage),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
