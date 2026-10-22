package com.swipeclean.app.data

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager

object StorageHelper {

    fun resolveStorageInfo(context: Context, filePath: String?, volumeNameAttr: String?): Pair<StorageType, String> {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager

        // 1. Check path prefix for primary internal storage
        if (!filePath.isNullOrBlank()) {
            if (filePath.startsWith("/storage/emulated/0") || filePath.startsWith("/data/")) {
                return Pair(StorageType.INTERNAL, "ذاكرة الهاتف")
            }
        }

        // 2. Query StorageManager StorageVolumes (Android 7.0+)
        if (sm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val volumes = sm.storageVolumes
            for (vol in volumes) {
                val desc = vol.getDescription(context).orEmpty()
                val uuid = vol.uuid

                val matchesPath = (!filePath.isNullOrBlank() && !uuid.isNullOrBlank() && filePath.contains(uuid, ignoreCase = true)) ||
                        (!volumeNameAttr.isNullOrBlank() && !uuid.isNullOrBlank() && volumeNameAttr.equals(uuid, ignoreCase = true))

                if (matchesPath || (vol.isPrimary && (filePath == null || filePath.startsWith("/storage/emulated")))) {
                    if (vol.isPrimary) {
                        return Pair(StorageType.INTERNAL, "ذاكرة الهاتف")
                    }
                    val isUsb = desc.contains("USB", ignoreCase = true) ||
                            desc.contains("فلاشة", ignoreCase = true) ||
                            desc.contains("Drive", ignoreCase = true) ||
                            desc.contains("OTG", ignoreCase = true)
                    return if (isUsb) {
                        Pair(StorageType.USB_DRIVE, if (desc.isNotBlank()) desc else "وحدة USB خارجية")
                    } else {
                        Pair(StorageType.SD_CARD, if (desc.isNotBlank()) desc else "بطاقة الذاكرة (SD)")
                    }
                }
            }
        }

        // 3. Fallback heuristic from file path
        if (!filePath.isNullOrBlank()) {
            val lower = filePath.lowercase()
            if (lower.contains("usb") || lower.contains("otg")) {
                return Pair(StorageType.USB_DRIVE, "وحدة USB خارجية")
            }
            if (filePath.startsWith("/storage/") && !filePath.startsWith("/storage/emulated")) {
                val parts = filePath.split("/")
                val volumeId = if (parts.size > 2) parts[2] else "SD"
                return Pair(StorageType.SD_CARD, "بطاقة الذاكرة ($volumeId)")
            }
        }

        return Pair(StorageType.INTERNAL, "ذاكرة الهاتف")
    }

    fun getAvailableStorageTypes(items: List<MediaItem>): List<StorageTypeInfo> {
        val presentTypes = items.map { it.storageType }.distinct()
        val list = mutableListOf<StorageTypeInfo>()

        list.add(
            StorageTypeInfo(
                type = StorageType.ALL,
                labelAr = "كل وحدات التخزين",
                labelEn = "All Storage",
                emoji = "📁",
                count = items.size
            )
        )

        val internalCount = items.count { it.storageType == StorageType.INTERNAL }
        list.add(
            StorageTypeInfo(
                type = StorageType.INTERNAL,
                labelAr = "ذاكرة الهاتف",
                labelEn = "Phone Storage",
                emoji = "📱",
                count = internalCount
            )
        )

        val sdCount = items.count { it.storageType == StorageType.SD_CARD }
        if (sdCount > 0 || StorageType.SD_CARD in presentTypes) {
            list.add(
                StorageTypeInfo(
                    type = StorageType.SD_CARD,
                    labelAr = "بطاقة الذاكرة (SD)",
                    labelEn = "SD Card",
                    emoji = "💾",
                    count = sdCount
                )
            )
        }

        val usbCount = items.count { it.storageType == StorageType.USB_DRIVE }
        if (usbCount > 0 || StorageType.USB_DRIVE in presentTypes) {
            list.add(
                StorageTypeInfo(
                    type = StorageType.USB_DRIVE,
                    labelAr = "فلاشة / هارد خارجي (USB)",
                    labelEn = "USB / External HDD",
                    emoji = "🔌",
                    count = usbCount
                )
            )
        }

        return list
    }
}
