package com.swipeclean.app.data

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BatchDeleteResult(
    val deletedCount: Int,
    val intentSender: IntentSender? = null
)

class MediaRepository(private val context: Context) {

    suspend fun loadMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaItems = mutableListOf<MediaItem>()
        mediaItems.addAll(loadImages())
        mediaItems.addAll(loadVideos())
        mediaItems.sortedByDescending { it.dateAdded }
    }

    private fun loadImages(): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection, projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "photo_$id.jpg"
                val date = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeColumn) ?: "image/*"
                val bucketName = cursor.getString(bucketColumn) ?: ""
                val filePath = cursor.getString(dataColumn) ?: ""

                val (storageType, storageVolName) = StorageHelper.resolveStorageInfo(
                    context = context,
                    filePath = filePath,
                    volumeNameAttr = null
                )

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                images.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = name,
                        type = MediaType.IMAGE,
                        dateAdded = date,
                        size = size,
                        mimeType = mimeType,
                        bucketName = bucketName,
                        storageType = storageType,
                        storageVolumeName = storageVolName,
                        filePath = filePath
                    )
                )
            }
        }
        return images
    }

    private fun loadVideos(): List<MediaItem> {
        val videos = mutableListOf<MediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection, projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "video_$id.mp4"
                val date = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                val mimeType = cursor.getString(mimeColumn) ?: "video/*"
                val bucketName = cursor.getString(bucketColumn) ?: ""
                val filePath = cursor.getString(dataColumn) ?: ""

                val (storageType, storageVolName) = StorageHelper.resolveStorageInfo(
                    context = context,
                    filePath = filePath,
                    volumeNameAttr = null
                )

                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )

                videos.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = name,
                        type = MediaType.VIDEO,
                        dateAdded = date,
                        size = size,
                        duration = duration,
                        mimeType = mimeType,
                        bucketName = bucketName,
                        storageType = storageType,
                        storageVolumeName = storageVolName,
                        filePath = filePath
                    )
                )
            }
        }
        return videos
    }

    suspend fun deleteMediaBatch(items: List<MediaItem>): BatchDeleteResult = withContext(Dispatchers.IO) {
        val uris = items.map { it.uri }
        var deleted = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(context.contentResolver, uris)
            return@withContext BatchDeleteResult(deletedCount = 0, intentSender = pi.intentSender)
        }

        for (item in items) {
            try {
                val rows = context.contentResolver.delete(item.uri, null, null)
                if (rows > 0) deleted++
            } catch (sec: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val rse = sec as? RecoverableSecurityException
                    if (rse != null) {
                        return@withContext BatchDeleteResult(
                            deletedCount = deleted,
                            intentSender = rse.userAction.actionIntent.intentSender
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        BatchDeleteResult(deletedCount = deleted)
    }
}
