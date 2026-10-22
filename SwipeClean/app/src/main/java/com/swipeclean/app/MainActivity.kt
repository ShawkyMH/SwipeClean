package com.swipeclean.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swipeclean.app.data.AppLanguage
import com.swipeclean.app.data.AppStrings
import com.swipeclean.app.data.AppTheme
import com.swipeclean.app.ui.screens.GalleryScreen
import com.swipeclean.app.ui.screens.SettingsScreen
import com.swipeclean.app.ui.screens.SwipeScreen
import com.swipeclean.app.ui.screens.TrashScreen
import com.swipeclean.app.ui.theme.DeleteRed
import com.swipeclean.app.ui.theme.SwipeCleanTheme
import com.swipeclean.app.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MediaViewModel = viewModel()
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            SwipeCleanTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeCleanApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SwipeCleanApp(viewModel: MediaViewModel = viewModel()) {
    val navController = rememberNavController()

    val filteredItems by viewModel.filteredMediaItems.collectAsState()
    val sortedTrashItems by viewModel.sortedTrashItems.collectAsState()
    val trashItems by viewModel.trashItems.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val keptCount by viewModel.keptCount.collectAsState()
    val intentSender by viewModel.intentSenderForDelete.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val availableFolders by viewModel.availableFolders.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val trashSortOrder by viewModel.trashSortOrder.collectAsState()

    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val fullscreenSwipeMode by viewModel.fullscreenSwipeMode.collectAsState()
    val selectedStorageType by viewModel.selectedStorageType.collectAsState()
    val availableStorageTypes by viewModel.availableStorageTypes.collectAsState()

    // Handle delete permission dialog for Android 10+ / 11+
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            viewModel.onDeletePermissionGranted()
        } else {
            viewModel.onDeletePermissionDenied()
        }
    }

    LaunchedEffect(intentSender) {
        intentSender?.let { sender ->
            deleteLauncher.launch(
                IntentSenderRequest.Builder(sender).build()
            )
            viewModel.clearIntentSender()
        }
    }

    // Permission handling
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission) {
            viewModel.loadMedia()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        hasPermission = permissions.all { perm ->
            ContextCompat.checkSelfPermission(
                viewModel.getApplication(),
                perm
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            viewModel.loadMedia()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    if (language == AppLanguage.ARABIC) "نحتاج إذن الوصول للصور والفيديوهات" else "Media Permission Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (language == AppLanguage.ARABIC)
                        "من فضلك اسمح للتطبيق بالوصول للصور والفيديوهات حتى يتمكن من عرضها ومساعدتك في تنظيفها."
                    else
                        "Please allow the app to access photos and videos so you can review and clean them.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(permissions)
                    }
                ) {
                    Text(if (language == AppLanguage.ARABIC) "اسمح بالوصول" else "Grant Permission")
                }
            }
        }
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "swipe"
        val showBottomBar = currentRoute in listOf("swipe", "gallery", "trash")

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        // Tab 1: Clean (تنظيف)
                        NavigationBarItem(
                            selected = currentRoute == "swipe",
                            onClick = {
                                if (currentRoute != "swipe") {
                                    navController.navigate("swipe") {
                                        popUpTo("swipe") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Default.ViewCarousel,
                                    contentDescription = AppStrings.clean(language)
                                )
                            },
                            label = { Text(AppStrings.clean(language)) }
                        )

                        // Tab 2: Gallery (المعرض)
                        NavigationBarItem(
                            selected = currentRoute == "gallery",
                            onClick = {
                                if (currentRoute != "gallery") {
                                    navController.navigate("gallery") {
                                        popUpTo("swipe") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = AppStrings.gallery(language)
                                )
                            },
                            label = { Text(AppStrings.gallery(language)) }
                        )

                        // Tab 3: Trash (السلة)
                        NavigationBarItem(
                            selected = currentRoute == "trash",
                            onClick = {
                                if (currentRoute != "trash") {
                                    navController.navigate("trash") {
                                        popUpTo("swipe") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (trashItems.isNotEmpty()) {
                                            Badge(
                                                containerColor = DeleteRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("${trashItems.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = AppStrings.trashTab(language)
                                    )
                                }
                            },
                            label = { Text(AppStrings.trashTab(language)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = "swipe"
                ) {
                    composable("swipe") {
                        SwipeScreen(
                            mediaItems = filteredItems,
                            currentIndex = currentIndex,
                            trashCount = trashItems.size,
                            keptCount = keptCount,
                            isLoading = isLoading,
                            filterType = filterType,
                            selectedFolder = selectedFolder,
                            availableFolders = availableFolders,
                            sortOrder = sortOrder,
                            selectedStorageType = selectedStorageType,
                            availableStorageTypes = availableStorageTypes,
                            currentLanguage = language,
                            fullscreenSwipeMode = fullscreenSwipeMode,
                            onSwipeRight = { viewModel.keepCurrentItem() },
                            onSwipeLeft = { viewModel.moveCurrentItemToTrash() },
                            onKeepItem = { viewModel.keepItem(it) },
                            onTrashItem = { viewModel.moveItemToTrash(it) },
                            onIndexChanged = { viewModel.updateCurrentIndex(it) },
                            onNavigateToGallery = { navController.navigate("gallery") },
                            onNavigateToTrash = { navController.navigate("trash") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onFilterTypeChanged = { viewModel.setFilterType(it) },
                            onStorageTypeChanged = { viewModel.setSelectedStorageType(it) },
                            onFolderSelected = { viewModel.setSelectedFolder(it) },
                            onSortOrderChanged = { viewModel.setSortOrder(it) }
                        )
                    }
                    composable("gallery") {
                        GalleryScreen(
                            mediaItems = filteredItems,
                            isLoading = isLoading,
                            onItemClick = { item ->
                                viewModel.selectItemAndStartFrom(item)
                                navController.navigate("swipe") {
                                    popUpTo("swipe") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate("swipe") {
                                        popUpTo("swipe") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                    composable("trash") {
                        TrashScreen(
                            trashItems = sortedTrashItems,
                            isDeleting = isDeleting,
                            trashSortOrder = trashSortOrder,
                            onSortOrderChanged = { viewModel.setTrashSortOrder(it) },
                            onRestoreItem = { viewModel.restoreFromTrash(it) },
                            onEmptyTrash = { viewModel.emptyTrash() },
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate("swipe") {
                                        popUpTo("swipe") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            currentLanguage = language,
                            currentTheme = theme,
                            currentFullscreenSwipeMode = fullscreenSwipeMode,
                            availableStorageTypes = availableStorageTypes,
                            onLanguageChanged = { viewModel.setLanguage(it) },
                            onThemeChanged = { viewModel.setTheme(it) },
                            onFullscreenSwipeModeChanged = { viewModel.setFullscreenSwipeMode(it) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
