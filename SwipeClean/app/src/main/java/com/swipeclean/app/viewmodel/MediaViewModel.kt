package com.swipeclean.app.viewmodel

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.swipeclean.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import java.io.File

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    val settingsManager = SettingsManager(application)
    private val updateManager = UpdateManager(application)

    val language: StateFlow<AppLanguage> = settingsManager.language
    val theme: StateFlow<AppTheme> = settingsManager.theme
    val fullscreenSwipeMode: StateFlow<FullscreenSwipeMode> = settingsManager.fullscreenSwipeMode

    // App Update States (GitHub)
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateCheckMessage = MutableStateFlow<String?>(null)
    val updateCheckMessage: StateFlow<String?> = _updateCheckMessage.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _trashItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val trashItems: StateFlow<List<MediaItem>> = _trashItems.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _keptCount = MutableStateFlow(0)
    val keptCount: StateFlow<Int> = _keptCount.asStateFlow()

    private val _intentSenderForDelete = MutableStateFlow<IntentSender?>(null)
    val intentSenderForDelete: StateFlow<IntentSender?> = _intentSenderForDelete.asStateFlow()

    // Filter, Storage & Sort states
    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    private val _selectedStorageType = MutableStateFlow(StorageType.ALL)
    val selectedStorageType: StateFlow<StorageType> = _selectedStorageType.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _trashSortOrder = MutableStateFlow(TrashSortOrder.RECENTLY_ADDED)
    val trashSortOrder: StateFlow<TrashSortOrder> = _trashSortOrder.asStateFlow()

    init {
        // Silently check for updates from GitHub in background on launch
        checkForUpdates(isManual = false)
    }

    private val _availableFolders = MutableStateFlow<List<String>>(emptyList())
    val availableFolders: StateFlow<List<String>> = _availableFolders.asStateFlow()

    // Dynamic storage drives info (Internal, SD Card, USB)
    val availableStorageTypes: StateFlow<List<StorageTypeInfo>> = _mediaItems.map { items ->
        StorageHelper.getAvailableStorageTypes(items)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private data class FilterConfig(
        val filter: FilterType,
        val storage: StorageType,
        val folder: String?,
        val sort: SortOrder
    )

    private val _filterConfig: Flow<FilterConfig> = combine(
        _filterType, _selectedStorageType, _selectedFolder, _sortOrder
    ) { filter, storage, folder, sort ->
        FilterConfig(filter, storage, folder, sort)
    }

    // Filtered & Sorted items
    val filteredMediaItems: StateFlow<List<MediaItem>> = combine(
        _mediaItems, _trashItems, _filterConfig
    ) { items, trash, config ->
        val trashIds = trash.map { it.id }.toSet()
        val list = items.filter { it.id !in trashIds }.filter { item ->
            val typeMatch = when (config.filter) {
                FilterType.ALL -> true
                FilterType.IMAGE -> item.type == MediaType.IMAGE
                FilterType.VIDEO -> item.type == MediaType.VIDEO
            }
            val storageMatch = when (config.storage) {
                StorageType.ALL -> true
                else -> item.storageType == config.storage
            }
            val folderMatch = config.folder == null || item.bucketName == config.folder
            typeMatch && storageMatch && folderMatch
        }

        when (config.sort) {
            SortOrder.NEWEST_FIRST -> list.sortedByDescending { it.dateAdded }
            SortOrder.OLDEST_FIRST -> list.sortedBy { it.dateAdded }
            SortOrder.LARGEST_FIRST -> list.sortedByDescending { it.size }
            SortOrder.SMALLEST_FIRST -> list.sortedBy { it.size }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Sorted Trash Items
    val sortedTrashItems: StateFlow<List<MediaItem>> = combine(
        _trashItems, _trashSortOrder
    ) { trash, sort ->
        when (sort) {
            TrashSortOrder.RECENTLY_ADDED -> trash.sortedByDescending { it.trashTimestamp }
            TrashSortOrder.OLDEST_ADDED -> trash.sortedBy { it.trashTimestamp }
            TrashSortOrder.LARGEST_SIZE -> trash.sortedByDescending { it.size }
            TrashSortOrder.SMALLEST_SIZE -> trash.sortedBy { it.size }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun getFilteredItems(): List<MediaItem> {
        val trashIds = _trashItems.value.map { it.id }.toSet()
        val filter = _filterType.value
        val storage = _selectedStorageType.value
        val folder = _selectedFolder.value
        val sort = _sortOrder.value
        val list = _mediaItems.value.filter { it.id !in trashIds }.filter { item ->
            val typeMatch = when (filter) {
                FilterType.ALL -> true
                FilterType.IMAGE -> item.type == MediaType.IMAGE
                FilterType.VIDEO -> item.type == MediaType.VIDEO
            }
            val storageMatch = when (storage) {
                StorageType.ALL -> true
                else -> item.storageType == storage
            }
            val folderMatch = folder == null || item.bucketName == folder
            typeMatch && storageMatch && folderMatch
        }

        return when (sort) {
            SortOrder.NEWEST_FIRST -> list.sortedByDescending { it.dateAdded }
            SortOrder.OLDEST_FIRST -> list.sortedBy { it.dateAdded }
            SortOrder.LARGEST_FIRST -> list.sortedByDescending { it.size }
            SortOrder.SMALLEST_FIRST -> list.sortedBy { it.size }
        }
    }

    fun loadMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = repository.loadMedia()
                _mediaItems.value = items
                _availableFolders.value = items.map { it.bucketName }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                _currentIndex.value = 0
                prefetchNextItems(0)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun prefetchNextItems(currentIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = getFilteredItems()
            val prefetchCount = 3
            for (i in 1..prefetchCount) {
                val nextIdx = currentIndex + i
                if (nextIdx < list.size) {
                    val nextItem = list[nextIdx]
                    if (nextItem.type == MediaType.IMAGE) {
                        val request = ImageRequest.Builder(getApplication())
                            .data(nextItem.uri)
                            .memoryCacheKey(nextItem.uri.toString())
                            .build()
                        getApplication<Application>().imageLoader.enqueue(request)
                    }
                }
            }
        }
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
        _currentIndex.value = 0
        prefetchNextItems(0)
    }

    fun setSelectedStorageType(type: StorageType) {
        _selectedStorageType.value = type
        _currentIndex.value = 0
        prefetchNextItems(0)
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = folder
        _currentIndex.value = 0
        prefetchNextItems(0)
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _currentIndex.value = 0
        prefetchNextItems(0)
    }

    fun setTrashSortOrder(order: TrashSortOrder) {
        _trashSortOrder.value = order
    }

    fun setLanguage(lang: AppLanguage) {
        settingsManager.setLanguage(lang)
    }

    fun setTheme(theme: AppTheme) {
        settingsManager.setTheme(theme)
    }

    fun setFullscreenSwipeMode(mode: FullscreenSwipeMode) {
        settingsManager.setFullscreenSwipeMode(mode)
    }

    fun keepCurrentItem() {
        _keptCount.value++
        moveToNext()
    }

    fun keepItem(item: MediaItem) {
        _keptCount.value++
        val filtered = getFilteredItems()
        val index = filtered.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            _currentIndex.value = (index + 1).coerceAtMost(filtered.size)
        }
    }

    fun moveCurrentItemToTrash() {
        val filtered = getFilteredItems()
        val index = _currentIndex.value
        if (index >= filtered.size) return

        val item = filtered[index].copy(trashTimestamp = System.currentTimeMillis())
        _trashItems.value = _trashItems.value + item

        val newFiltered = getFilteredItems()
        if (newFiltered.isEmpty()) {
            _currentIndex.value = 0
        } else if (_currentIndex.value >= newFiltered.size) {
            _currentIndex.value = newFiltered.size - 1
        }
        prefetchNextItems(_currentIndex.value)
    }

    fun moveItemToTrash(item: MediaItem) {
        val itemWithTime = item.copy(trashTimestamp = System.currentTimeMillis())
        _trashItems.value = _trashItems.value + itemWithTime

        val newFiltered = getFilteredItems()
        if (newFiltered.isEmpty()) {
            _currentIndex.value = 0
        } else if (_currentIndex.value >= newFiltered.size) {
            _currentIndex.value = (newFiltered.size - 1).coerceAtLeast(0)
        }
        prefetchNextItems(_currentIndex.value)
    }

    fun restoreFromTrash(item: MediaItem) {
        _trashItems.value = _trashItems.value.filter { it.id != item.id }
        prefetchNextItems(_currentIndex.value)
    }

    fun emptyTrash() {
        val itemsToDelete = _trashItems.value
        if (itemsToDelete.isEmpty()) return

        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val result = repository.deleteMediaBatch(itemsToDelete)
                if (result.intentSender != null) {
                    _intentSenderForDelete.value = result.intentSender
                } else {
                    val deletedIds = itemsToDelete.take(result.deletedCount).map { it.id }.toSet()
                    _mediaItems.value = _mediaItems.value.filter { it.id !in deletedIds }
                    _trashItems.value = _trashItems.value.filter { it.id !in deletedIds }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun onDeletePermissionGranted() {
        val deletedIds = _trashItems.value.map { it.id }.toSet()
        _mediaItems.value = _mediaItems.value.filter { it.id !in deletedIds }
        _trashItems.value = emptyList()
        _intentSenderForDelete.value = null
    }

    fun onDeletePermissionDenied() {
        _intentSenderForDelete.value = null
    }

    fun selectItemAndStartFrom(item: MediaItem) {
        val filtered = getFilteredItems()
        val index = filtered.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            _currentIndex.value = index
            prefetchNextItems(index)
        }
    }

    fun updateCurrentIndex(index: Int) {
        val filtered = getFilteredItems()
        if (index in 0..filtered.size) {
            _currentIndex.value = index
            prefetchNextItems(index)
        }
    }

    private fun moveToNext() {
        val filtered = getFilteredItems()
        if (_currentIndex.value < filtered.size) {
            _currentIndex.value++
            prefetchNextItems(_currentIndex.value)
        }
    }

    fun clearIntentSender() {
        _intentSenderForDelete.value = null
    }

    fun getInstalledVersionName(): String = updateManager.getInstalledVersionName()
    fun getInstalledVersionCode(): Long = updateManager.getInstalledVersionCode()

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateCheckMessage.value = null
            try {
                val result = updateManager.checkUpdate()
                result.onSuccess { update ->
                    if (update != null) {
                        _updateInfo.value = update
                        updateManager.showUpdateNotification(update, language.value)
                    } else if (isManual) {
                        _updateCheckMessage.value = if (language.value == AppLanguage.ARABIC)
                            "أنت تستخدم أحدث إصدار بالفعل! (v${getInstalledVersionName()})"
                        else
                            "You are already on the latest version! (v${getInstalledVersionName()})"
                    }
                }.onFailure {
                    if (isManual) {
                        _updateCheckMessage.value = if (language.value == AppLanguage.ARABIC)
                            "تعذر الاتصال بـ GitHub للتحقق من التحديثات. تأكد من اتصالك بالإنترنت."
                        else
                            "Could not check updates on GitHub. Check your internet connection."
                    }
                }
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun startDownload(updateInfo: AppUpdateInfo) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading(0f, 0L, 0L)
            val result = updateManager.downloadApk(
                apkUrl = updateInfo.downloadUrl,
                versionName = updateInfo.versionName
            ) { progress, downloaded, total ->
                _downloadState.value = DownloadState.Downloading(progress, downloaded, total)
            }
            result.onSuccess { file ->
                _downloadState.value = DownloadState.Completed(file)
            }.onFailure { error ->
                _downloadState.value = DownloadState.Error(error.localizedMessage ?: "Download failed")
            }
        }
    }

    fun installDownloadedApk(apkFile: File) {
        updateManager.installApk(apkFile)
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
        _downloadState.value = DownloadState.Idle
    }

    fun clearUpdateMessage() {
        _updateCheckMessage.value = null
    }
}
