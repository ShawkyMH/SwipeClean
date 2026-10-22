package com.swipeclean.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swipeclean.app.data.FullscreenSwipeMode
import com.swipeclean.app.data.MediaItem
import com.swipeclean.app.data.MediaType
import com.swipeclean.app.ui.theme.DeleteRed
import com.swipeclean.app.ui.theme.KeepGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun FullscreenMediaDialog(
    mediaItems: List<MediaItem>,
    initialIndex: Int,
    fullscreenSwipeMode: FullscreenSwipeMode,
    onDismiss: () -> Unit,
    onKeepItem: (MediaItem) -> Unit,
    onTrashItem: (MediaItem) -> Unit,
    onIndexChanged: (Int) -> Unit
) {
    if (mediaItems.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, mediaItems.size - 1)) }
    val currentItem = mediaItems.getOrNull(activeIndex) ?: mediaItems.first()

    // Zoom & Pan state for images
    var scale by remember(currentItem.id) { mutableFloatStateOf(1f) }
    var panOffsetX by remember(currentItem.id) { mutableFloatStateOf(0f) }
    var panOffsetY by remember(currentItem.id) { mutableFloatStateOf(0f) }

    // Swipe offset for Clean mode
    var swipeOffsetX by remember(currentItem.id) { mutableFloatStateOf(0f) }
    var isSwipingOut by remember(currentItem.id) { mutableStateOf(false) }

    // Video state
    var isMuted by remember(currentItem.id) { mutableStateOf(false) }
    var isPlaying by remember(currentItem.id) { mutableStateOf(true) }

    // Video progress & duration scrubber states
    var currentPositionMs by remember(currentItem.id) { mutableLongStateOf(0L) }
    var durationMs by remember(currentItem.id) { mutableLongStateOf(0L) }
    var isScrubbing by remember(currentItem.id) { mutableStateOf(false) }
    var scrubPositionMs by remember(currentItem.id) { mutableLongStateOf(0L) }

    val exoPlayer = remember(currentItem.id) {
        if (currentItem.type == MediaType.VIDEO) {
            ExoPlayer.Builder(context).build().apply {
                val item = ExoMediaItem.fromUri(currentItem.uri)
                setMediaItem(item)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f
                prepare()
                playWhenReady = true
            }
        } else null
    }

    DisposableEffect(currentItem.id) {
        onDispose {
            exoPlayer?.release()
        }
    }

    // Continuously sync video progress slider with ExoPlayer current position
    LaunchedEffect(currentItem.id, exoPlayer, isPlaying) {
        val player = exoPlayer ?: return@LaunchedEffect
        while (true) {
            if (!isScrubbing) {
                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                val d = player.duration
                if (d > 0L) {
                    durationMs = d
                }
            }
            delay(120L)
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer?.play() else exoPlayer?.pause()
    }

    LaunchedEffect(activeIndex) {
        onIndexChanged(activeIndex)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main media viewer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(swipeOffsetX.roundToInt(), 0) }
                    .pointerInput(currentItem.id, fullscreenSwipeMode, scale) {
                        if (fullscreenSwipeMode == FullscreenSwipeMode.BROWSE_ONLY) {
                            // BROWSE ONLY MODE:
                            // If scale <= 1.05f, horizontal drags browse to previous / next item
                            // If scale > 1.05f, transform gestures pan and zoom
                            if (scale <= 1.05f) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (swipeOffsetX > 90f && activeIndex > 0) {
                                            // Browse to Previous
                                            activeIndex--
                                        } else if (swipeOffsetX < -90f && activeIndex < mediaItems.size - 1) {
                                            // Browse to Next
                                            activeIndex++
                                        }
                                        swipeOffsetX = 0f
                                    },
                                    onDragCancel = { swipeOffsetX = 0f },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        swipeOffsetX += dragAmount.x
                                    }
                                )
                            }
                        } else {
                            // CLEAN ACTIONS MODE:
                            // Swiping left moves to trash, swiping right keeps
                            if (isSwipingOut) return@pointerInput
                            detectDragGestures(
                                onDragEnd = {
                                    val currentX = swipeOffsetX
                                    if (currentX > 140f) {
                                        isSwipingOut = true
                                        coroutineScope.launch {
                                            onKeepItem(currentItem)
                                            if (activeIndex < mediaItems.size - 1) {
                                                activeIndex++
                                            } else {
                                                onDismiss()
                                            }
                                            swipeOffsetX = 0f
                                            isSwipingOut = false
                                        }
                                    } else if (currentX < -140f) {
                                        isSwipingOut = true
                                        coroutineScope.launch {
                                            onTrashItem(currentItem)
                                            if (activeIndex >= mediaItems.size - 1) {
                                                activeIndex = (mediaItems.size - 2).coerceAtLeast(0)
                                            }
                                            swipeOffsetX = 0f
                                            isSwipingOut = false
                                        }
                                    } else {
                                        swipeOffsetX = 0f
                                    }
                                },
                                onDragCancel = { swipeOffsetX = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    swipeOffsetX += dragAmount.x
                                }
                            )
                        }
                    }
                    .pointerInput(currentItem.id) {
                        // Pinch to zoom and pan for photos and videos
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1.05f) {
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            } else {
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                        }
                    }
                    .pointerInput(currentItem.id) {
                        // Double tap zoom for photos and videos
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (currentItem.type == MediaType.VIDEO && exoPlayer != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setOnTouchListener { _, _ -> false }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = panOffsetX,
                                translationY = panOffsetY
                            )
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentItem.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentItem.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = panOffsetX,
                                translationY = panOffsetY
                            )
                    )
                }

                // Keep / Trash Badges in Clean Mode
                if (fullscreenSwipeMode == FullscreenSwipeMode.CLEAN_ACTIONS) {
                    if (swipeOffsetX > 30f) {
                        val progress = (swipeOffsetX / 140f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(KeepGreen.copy(alpha = progress * 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = KeepGreen,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Text("احتفظ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    } else if (swipeOffsetX < -30f) {
                        val progress = (swipeOffsetX.absoluteValue / 140f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DeleteRed.copy(alpha = progress * 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = DeleteRed,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                    Text("إلى السلة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (fullscreenSwipeMode == FullscreenSwipeMode.BROWSE_ONLY)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else
                            KeepGreen.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = if (fullscreenSwipeMode == FullscreenSwipeMode.BROWSE_ONLY) "معرض تصفح 🖼️" else "وضع تنظيف 🧹",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // File Counter
                Text(
                    text = "${activeIndex + 1} / ${mediaItems.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Action buttons (Reset zoom or video controls)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (scale > 1.1f) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "إعادة ضبط", tint = Color.White)
                        }
                    }

                    if (currentItem.type == MediaType.VIDEO) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل/إيقاف",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { isMuted = !isMuted },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isMuted) DeleteRed.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "الصوت",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Navigation & Video Controls Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(bottom = 12.dp)
            ) {
                // Video Progress Slider and timestamps (Only displayed for videos)
                if (currentItem.type == MediaType.VIDEO && exoPlayer != null) {
                    val displayPosition = if (isScrubbing) scrubPositionMs else currentPositionMs
                    val totalDuration = if (durationMs > 0L) durationMs else currentItem.duration.coerceAtLeast(1L)
                    val sliderProgress = (displayPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatVideoTime(displayPosition),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatVideoTime(totalDuration),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Slider(
                            value = sliderProgress,
                            onValueChange = { newProgress ->
                                isScrubbing = true
                                scrubPositionMs = (newProgress * totalDuration).toLong()
                            },
                            onValueChangeFinished = {
                                exoPlayer.seekTo(scrubPositionMs)
                                currentPositionMs = scrubPositionMs
                                isScrubbing = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFA855F7),
                                activeTrackColor = Color(0xFFA855F7),
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )
                    }
                }

                // Row with Previous, Metadata, Next
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    IconButton(
                        onClick = { if (activeIndex > 0) activeIndex-- },
                        enabled = activeIndex > 0,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "السابق",
                            tint = if (activeIndex > 0) Color.White else Color.Gray
                        )
                    }

                    // File metadata
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentItem.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (currentItem.type == MediaType.VIDEO) "🎬 فيديو • " else "📷 صورة • "}${formatFileSize(currentItem.size)}${if (currentItem.storageVolumeName.isNotBlank()) " • 💾 ${currentItem.storageVolumeName}" else ""}",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }

                    // Next button
                    IconButton(
                        onClick = { if (activeIndex < mediaItems.size - 1) activeIndex++ },
                        enabled = activeIndex < mediaItems.size - 1,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "التالي",
                            tint = if (activeIndex < mediaItems.size - 1) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Format milliseconds to mm:ss timestamp display for video slider
fun formatVideoTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
