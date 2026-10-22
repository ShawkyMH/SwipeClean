package com.swipeclean.app.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAdded: Long,
    val size: Long,
    val duration: Long = 0L,
    val mimeType: String,
    val bucketName: String = "",
    val trashTimestamp: Long = 0L,
    val storageType: StorageType = StorageType.INTERNAL,
    val storageVolumeName: String = "ذاكرة الهاتف",
    val filePath: String = ""
)

enum class MediaType {
    IMAGE, VIDEO
}

enum class StorageType(val labelAr: String, val labelEn: String, val emoji: String) {
    ALL("كل وحدات التخزين", "All Storage", "📁"),
    INTERNAL("ذاكرة الهاتف", "Phone Storage", "📱"),
    SD_CARD("بطاقة الذاكرة (SD)", "SD Card", "💾"),
    USB_DRIVE("وحدة خارجية (USB)", "USB / HDD Drive", "🔌")
}

data class StorageTypeInfo(
    val type: StorageType,
    val labelAr: String,
    val labelEn: String,
    val emoji: String,
    val count: Int
)

enum class FilterType(val label: String) {
    ALL("الكل"),
    IMAGE("📷 صور"),
    VIDEO("🎬 فيديو")
}

enum class SortOrder(val labelAr: String, val labelEn: String) {
    NEWEST_FIRST("الأحدث أولاً 🕒", "Newest First 🕒"),
    OLDEST_FIRST("الأقدم أولاً ⏳", "Oldest First ⏳"),
    LARGEST_FIRST("الأكبر حجماً 🚀", "Largest First 🚀"),
    SMALLEST_FIRST("الأصغر حجماً 📦", "Smallest First 📦");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) labelAr else labelEn
}

enum class TrashSortOrder(val labelAr: String, val labelEn: String) {
    RECENTLY_ADDED("أحدث ما أُضيف للسلة 🕒", "Recently Added 🕒"),
    OLDEST_ADDED("أقدم ما أُضيف للسلة ⏳", "Oldest Added ⏳"),
    LARGEST_SIZE("الأكبر حجماً 🚀", "Largest Size 🚀"),
    SMALLEST_SIZE("الأصغر حجماً 📦", "Smallest Size 📦");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) labelAr else labelEn
}
